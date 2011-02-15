package nu.fgv.register.reports.web;

import nu.fgv.register.reports.service.GeneratorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class GeneratorController {
    
    @Autowired
    private GeneratorService generatorService;

    @RequestMapping(value="/generate", method=RequestMethod.POST, headers="Accept=application/xml")
    public @ResponseBody String generate(@RequestBody String body) {
        System.out.println("body="+body);
        return "Hello world!";
    }

}
