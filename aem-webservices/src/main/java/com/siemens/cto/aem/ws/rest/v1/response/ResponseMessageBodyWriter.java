package com.siemens.cto.aem.ws.rest.v1.response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.util.StringUtils;


@Provider
@Produces(MediaType.TEXT_HTML)
public class ResponseMessageBodyWriter implements MessageBodyWriter<ApplicationResponse> {

    JacksonJsonProvider jsonProvider = new JacksonJsonProvider(); 
    
    public ResponseMessageBodyWriter() {
    }
    
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
            javax.ws.rs.core.MediaType mediaType) {        
        return ApplicationResponse.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(ApplicationResponse t, Class<?> type, Type genericType, Annotation[] annotations,
            javax.ws.rs.core.MediaType mediaType) {        
        return -1;
    }

    @Override
    public void writeTo(ApplicationResponse t, Class<?> type, Type genericType, Annotation[] annotations,
            javax.ws.rs.core.MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {
        
        ByteArrayOutputStream boss = new ByteArrayOutputStream();
        ObjectMapper om = jsonProvider.locateMapper(type, MediaType.APPLICATION_JSON_TYPE);
        if(om != null) {
            jsonProvider.writeTo(t, type, genericType, annotations, MediaType.APPLICATION_JSON_TYPE, httpHeaders, boss);
            boss.flush();
        }

        Writer osWriter = new OutputStreamWriter(entityStream);
        String code, text, prefix;
        if(StringUtils.hasText(t.getMsgCode())) { 
            code = "400"; prefix = t.getMsgCode();
        } else {
            code = "200"; prefix = "";
        }
        if(StringUtils.hasText(t.getMessage())) { 
            text = prefix + " " + t.getMessage();
        } else {
            text = "ok";
        }
        osWriter.write("<html><body status='"+code+"' statusText='" + text + "'>");
        osWriter.flush();
        entityStream.write(boss.toByteArray());
        boss.close();
        osWriter.write("</body></html>");
        osWriter.close();
    }

}
