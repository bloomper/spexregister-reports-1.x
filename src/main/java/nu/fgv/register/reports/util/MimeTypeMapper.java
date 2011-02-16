package nu.fgv.register.reports.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;

public class MimeTypeMapper {

    private Map<String, String> types = new HashMap<String, String>();
    
    @Required
    public void setTypes(Map<String, String> types) {
        this.types = types;
    }
    
    public String getType(String extension) {
        return types.get(extension.toLowerCase());
    }
}
