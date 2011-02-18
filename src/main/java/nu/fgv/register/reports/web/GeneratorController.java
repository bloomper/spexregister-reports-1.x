package nu.fgv.register.reports.web;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import nu.fgv.register.reports.service.GeneratorService;
import nu.fgv.register.reports.util.MimeTypeMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GeneratorController {

    final Logger log = LoggerFactory.getLogger(GeneratorController.class);

    @Autowired
    private GeneratorService generatorService;

    @Autowired
    private MimeTypeMapper mimeTypeMapper;

    @RequestMapping(value = "/generate", method = RequestMethod.POST)
    public void generate(@RequestParam("report") String report, @RequestParam("format") String format, @RequestParam(value = "selectCriteria", required = false) String selectCriteria, @RequestParam(value = "locale", required = false) String locale, @RequestBody String body, HttpServletResponse response) {
        try {
            log.debug("Request (report = {}, format = {}, locale = {}, selectCriteria = {}", new Object[] { report, format, locale, selectCriteria });
            byte[] generatedReport = generatorService.generate(report, format, locale, body, selectCriteria);
            if(generatedReport == null || generatedReport.length == 0) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
            response.setContentType(mimeTypeMapper.getType(format));
            response.setContentLength(generatedReport.length);
            log.debug("Response (contentType = {}, contentLength = {}", new Object[] { response.getContentType(), generatedReport.length });
            response.getOutputStream().write(generatedReport);
        } catch(Exception e) {
            log.error("Unexpected error", e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (IOException e1) {
                log.error("Unexpected error when sending error response", e1);
                throw new RuntimeException(e1);
            }
        }
    }

}
