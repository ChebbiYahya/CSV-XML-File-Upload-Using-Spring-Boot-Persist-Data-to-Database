package com.bank.uploadfileanddatapersistdb.service;

import com.bank.uploadfileanddatapersistdb.DTO.EmployeeDto;
import com.bank.uploadfileanddatapersistdb.config.EmployeeMappingProperties;
import com.bank.uploadfileanddatapersistdb.entity.LogChargement;
import com.bank.uploadfileanddatapersistdb.entity.enums.LineStatus;
import com.bank.uploadfileanddatapersistdb.interfaces.EmployeeService;
import com.bank.uploadfileanddatapersistdb.interfaces.FileIngestionService;
import com.bank.uploadfileanddatapersistdb.exception.*;
import com.bank.uploadfileanddatapersistdb.interfaces.LogChargementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.*;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FileIngestionServiceImpl implements FileIngestionService {
    private final EmployeeMappingProperties mappingProperties;
    private final EmployeeService employeeService;
    private final LogChargementService logChargementService;

//    public FileIngestionServiceImpl(EmployeeMappingProperties mappingProperties,
//                                    EmployeeService employeeService) {
//        this.mappingProperties = mappingProperties;
//        this.employeeService = employeeService;
//    }

    @Override
    public int ingestCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileFormatException("CSV file is empty");
        }

        // Création du log global pour ce fichier
        LogChargement log = logChargementService.startLog(file.getOriginalFilename());
        Set<Long> idsInFile = new HashSet<>();

        int successLines = 0;
        int failedLines = 0;
        int totalLines = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            EmployeeMappingProperties.CsvMapping csvMapping = mappingProperties.getCsv();
            String delimiter = csvMapping.getDelimiter();
            boolean hasHeader = csvMapping.isHasHeader();

            String line;
            int lineNumber = 0;
            List<EmployeeDto> employees = new ArrayList<>();

            Map<String, Integer> headerIndexMap = new HashMap<>();

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // ligne vide → on la loggue éventuellement comme ignorée ou on skip sans log
                if (line.trim().isEmpty()) {
                    continue;
                }

                if (lineNumber == 1 && hasHeader) {
                    String[] headers = line.split(delimiter);
                    for (int i = 0; i < headers.length; i++) {
                        String h = headers[i].trim();
                        headerIndexMap.put(h, i);
                    }
                    // on ne log pas la ligne header comme data, donc continue
                    validateCsvSchema(csvMapping, headerIndexMap);
                    continue;
                }

                // à partir d’ici, c’est une ligne de données
                totalLines++;

                try {
                    String[] tokens = line.split(delimiter);
                    EmployeeDto dto = new EmployeeDto();

                    for (EmployeeMappingProperties.CsvColumn column : csvMapping.getColumns()) {
                        Integer colIndex = null;

                        if (column.getHeader() != null) {
                            colIndex = headerIndexMap.get(column.getHeader());
                        } else {
                            colIndex = column.getIndex();
                        }

                        String rawValue = null;
                        if (colIndex != null && colIndex < tokens.length) {
                            rawValue = tokens[colIndex].trim();
                            if (rawValue.isEmpty()) {
                                rawValue = null;
                            }
                        }

                        applyField(dto,
                                column.getName(),
                                column.getType(),
                                column.isRequired(),   // 👈 NEW
                                rawValue,
                                lineNumber);
                    }

                    // si id null → on considère que la ligne est invalide
                    if (dto.getId() == null) {
                        throw new FileProcessingException("Employee id is null at line " + lineNumber);
                    }


                    // doublon dans le même fichier
                    if (idsInFile.contains(dto.getId())) {
                        failedLines++;
                        logChargementService.addLine(
                                log,
                                lineNumber,
                                LineStatus.FAILED,
                                "Duplicate employee id " + dto.getId() + " in CSV file (already seen in this file)"
                        );
                        continue;
                    }
                    idsInFile.add(dto.getId());

                    // doublon en base de données
                    if (employeeService.existsById(dto.getId())) {
                        failedLines++;
                        logChargementService.addLine(
                                log,
                                lineNumber,
                                LineStatus.FAILED,
                                "Employee with id " + dto.getId() + " already exists in database – row skipped"
                        );
                        continue;
                    }

                    employees.add(dto);

                    successLines++;
                    logChargementService.addLine(log, lineNumber, LineStatus.SUCCESS, null);

                } catch (Exception e) {
                    failedLines++;
                    logChargementService.addLine(
                            log,
                            lineNumber,
                            LineStatus.FAILED,
                            e.getMessage()
                    );
                    // on continue avec la ligne suivante
                }
            }

            // On persiste seulement les lignes réussies
            if (!employees.isEmpty()) {
                employeeService.saveAll(employees);
            }

            // finalisation du log global
            logChargementService.finalizeLog(log, totalLines, successLines, failedLines);

            return successLines;

        } catch (SchemaValidationException | ValidationException e) {
            // erreurs "métier" au niveau du fichier (schéma, required, format logique, etc.)
            logChargementService.addLine(
                    log,
                    0, // 0 = erreur niveau fichier, pas une ligne précise
                    LineStatus.FAILED,
                    "File-level error: " + e.getMessage()
            );
            logChargementService.finalizeLog(log, totalLines, successLines, failedLines);
            throw e; // on relance l’exception spécifique

        } catch (IOException e) {
            // erreurs de stream / IO
            logChargementService.addLine(
                    log,
                    0,
                    LineStatus.FAILED,
                    "Stream error while reading CSV: " + e.getMessage()
            );
            logChargementService.finalizeLog(log, totalLines, successLines, failedLines);
            throw new StreamProcessingException("Error reading CSV file: " + e.getMessage(), e);
        }
    }

    @Override
    public int ingestXml(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileFormatException("XML file is empty");
        }

        LogChargement log = logChargementService.startLog(file.getOriginalFilename());
        Set<Long> idsInFile = new HashSet<>();

        int successLines = 0;
        int failedLines = 0;
        int totalLines = 0;

        try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file.getInputStream());
            document.getDocumentElement().normalize();

            EmployeeMappingProperties.XmlMapping xmlMapping = mappingProperties.getXml();

            Element root = document.getDocumentElement();
            if (!xmlMapping.getRootElement().equals(root.getNodeName())) {
                throw new SchemaValidationException(
                        "Root element <" + root.getNodeName() + "> does not match expected <" +
                                xmlMapping.getRootElement() + ">");
            }

            NodeList nodeList = document.getElementsByTagName(xmlMapping.getRecordElement());

            List<EmployeeDto> employees = new ArrayList<>();

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE) continue;

                int lineNumber = i + 1; // "ligne" logique pour le détail
                totalLines++;

                try {
                    Element element = (Element) node;
                    EmployeeDto dto = new EmployeeDto();

                    for (EmployeeMappingProperties.XmlField field : xmlMapping.getFields()) {
                        String tagName = field.getTag();
                        NodeList values = element.getElementsByTagName(tagName);

                        String rawValue = null;
                        if (values.getLength() > 0) {
                            rawValue = values.item(0).getTextContent();
                            if (rawValue != null) {
                                rawValue = rawValue.trim();
                                if (rawValue.isEmpty()) {
                                    rawValue = null;
                                }
                            }
                        }

                        applyField(dto,
                                field.getName(),
                                field.getType(),
                                field.isRequired(),    // 👈 NEW
                                rawValue,
                                lineNumber);
                    }

                    if (dto.getId() == null) {
                        throw new FileProcessingException("Employee id is null at XML record index " + lineNumber);
                    }
                    // doublon dans le fichier
                    if (idsInFile.contains(dto.getId())) {
                        failedLines++;
                        logChargementService.addLine(
                                log,
                                lineNumber,
                                LineStatus.FAILED,
                                "Duplicate employee id " + dto.getId() + " in XML file (already seen in this file)"
                        );
                        continue;
                    }
                    idsInFile.add(dto.getId());

                    // doublon en base
                    if (employeeService.existsById(dto.getId())) {
                        failedLines++;
                        logChargementService.addLine(
                                log,
                                lineNumber,
                                LineStatus.FAILED,
                                "Employee with id " + dto.getId() + " already exists in database – record skipped"
                        );
                        continue;
                    }

                    employees.add(dto);

                    successLines++;
                    logChargementService.addLine(log, lineNumber, LineStatus.SUCCESS, null);

                } catch (Exception e) {
                    failedLines++;
                    logChargementService.addLine(
                            log,
                            lineNumber,
                            LineStatus.FAILED,
                            e.getMessage()
                    );
                }
            }

            if (!employees.isEmpty()) {
                employeeService.saveAll(employees);
            }

            logChargementService.finalizeLog(log, totalLines, successLines, failedLines);

            return successLines;

        } catch (SchemaValidationException | ValidationException e) {
            // erreurs de validation / schéma au niveau fichier
            logChargementService.addLine(
                    log,
                    0,
                    LineStatus.FAILED,
                    "File-level error: " + e.getMessage()
            );
            logChargementService.finalizeLog(log, totalLines, successLines, failedLines);
            throw e;

        } catch (Exception e) {
            // parse XML, IO, ou autres erreurs techniques
            logChargementService.addLine(
                    log,
                    0,
                    LineStatus.FAILED,
                    "Stream/XML error while reading file: " + e.getMessage()
            );
            logChargementService.finalizeLog(log, totalLines, successLines, failedLines);
            throw new StreamProcessingException("Error reading XML file: " + e.getMessage(), e);
        }
    }

    // ================== Helpers ==================

    private void applyField(EmployeeDto dto,
                            String fieldName,
                            String type,
                            boolean required,
                            String rawValue,
                            int index) {
        // 1) Champ manquant
        if (rawValue == null) {
            if (required) {
                throw new MissingRequiredFieldException(
                        "Required field '" + fieldName + "' is missing at line " + index);
            }

            // 2) Valeurs par défaut pour champs non required
            switch (fieldName) {
                case "salary" -> {
                    if (dto.getSalary() == null) {
                        dto.setSalary(BigDecimal.ZERO);
                    }
                }
                // tu peux fixer d'autres defaults ici
                // case "department" -> dto.setDepartment("UNKNOWN");
                default -> {
                    // rester à null
                }
            }
            return;
        }

        // 3) Parsing + type mismatch
        try {
            switch (fieldName) {
                case "id" -> dto.setId(parseLong(rawValue));
                case "firstName" -> dto.setFirstName(rawValue);
                case "lastName" -> dto.setLastName(rawValue);
                case "position" -> dto.setPosition(rawValue);
                case "department" -> dto.setDepartment(rawValue);
                case "hireDate" -> dto.setHireDate(parseLocalDate(rawValue));
                case "salary" -> dto.setSalary(parseDecimal(rawValue));
                default -> {
                    // pour futurs champs
                }
            }
        } catch (Exception e) {
            // on spécialise comme Format/Type mismatch
            throw new TypeMismatchException(
                    "Error parsing field '" + fieldName + "' with value '" + rawValue +
                            "' at line " + index + ": " + e.getMessage(), e);
        }
    }


    private Long parseLong(String value) {
        return Long.parseLong(value);
    }

    private LocalDate parseLocalDate(String value) {
        return LocalDate.parse(value); // yyyy-MM-dd
    }

    private BigDecimal parseDecimal(String value) {
        return new BigDecimal(value);
    }

    private void validateCsvSchema(EmployeeMappingProperties.CsvMapping csvMapping,
                                   Map<String, Integer> headerIndexMap) {

        if (!csvMapping.isHasHeader()) {
            return;
        }

        for (EmployeeMappingProperties.CsvColumn column : csvMapping.getColumns()) {
            if (column.isRequired() && column.getHeader() != null) {
                if (!headerIndexMap.containsKey(column.getHeader())) {
                    throw new SchemaValidationException(
                            "Required column '" + column.getHeader() + "' is not present in CSV header");
                }
            }
        }
    }
}
