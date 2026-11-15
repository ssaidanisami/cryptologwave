package com.cryptolog.wave.infrastructure.adapter.output.report;

import com.cryptolog.wave.domain.exception.ReportGenerationException;
import com.cryptolog.wave.domain.model.Employee;
import com.cryptolog.wave.domain.port.output.EmployeeReportPort;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Component;

@Component
public class JasperEmployeeReportAdapter implements EmployeeReportPort {

    private static final String REPORT_TEMPLATE_PATH = "/reports/employees_report.jrxml";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private volatile JasperReport cachedJasperReport;

    @Override
    public byte[] generateEmployeePdfReport(List<Employee> employees) {
        try {
            JasperReport jasperReport = getJasperReport();

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("reportTitle", "Employee Directory Report");
            parameters.put("generatedAt", LocalDateTime.now().format(DATE_TIME_FORMATTER));
            parameters.put("totalEmployees", employees != null ? employees.size() : 0);

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(
                    employees != null ? employees : List.of()
            );

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (JRException ex) {
            throw new ReportGenerationException("Failed to generate employee PDF report", ex);
        } catch (Exception ex) {
            throw new ReportGenerationException("Unexpected error during employee report generation", ex);
        }
    }

    private JasperReport getJasperReport() throws JRException {
        if (cachedJasperReport == null) {
            synchronized (this) {
                if (cachedJasperReport == null) {
                    try (InputStream reportStream = getClass().getResourceAsStream(REPORT_TEMPLATE_PATH)) {
                        if (reportStream == null) {
                            throw new ReportGenerationException(
                                    "Report template not found at classpath location: " + REPORT_TEMPLATE_PATH
                            );
                        }
                        cachedJasperReport = JasperCompileManager.compileReport(reportStream);
                    } catch (JRException ex) {
                        throw ex;
                    } catch (Exception ex) {
                        throw new ReportGenerationException("Failed to load report template: " + REPORT_TEMPLATE_PATH, ex);
                    }
                }
            }
        }
        return cachedJasperReport;
    }
}
