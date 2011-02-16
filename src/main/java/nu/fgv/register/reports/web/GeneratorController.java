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
    public void generate(@RequestParam("report") String report, @RequestParam("format") String format, @RequestParam(value = "selectCriteria", required = false, defaultValue = ".") String selectCriteria, @RequestBody String body, HttpServletResponse response) throws IOException {
        log.debug("Incoming request (report = {}, format = {}, selectCriteria = {}", new Object[] { report, format, selectCriteria });
        byte[] generatedReport = generatorService.generate(report, format, body, selectCriteria);
        response.setContentType(mimeTypeMapper.getType(format));
        response.setContentLength(generatedReport.length);
        log.debug("Outgoing response (contentType = {}, contentLength = {}", new Object[] { response.getContentType(), generatedReport.length });
        response.getOutputStream().write(generatedReport);
    }

}
