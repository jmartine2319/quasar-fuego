package com.jdmm.quasar;

import com.google.gson.Gson;
import com.jdmm.quasar.dto.ResultadoSatellitesOut;
import com.jdmm.quasar.dto.ResultadoUbicacion;
import com.jdmm.quasar.dto.SatellitesIn;
import com.jdmm.quasar.dto.Ubicacion;
import com.jdmm.quasar.logica.LogicaQuasar;
import com.jdmm.quasar.util.ExcepcionQasar;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Optional;

import org.apache.log4j.BasicConfigurator;

/**
 * Azure Functions with HTTP Trigger.
 */
public class ControladorQuasar {
    /**
     * This function listens at endpoint "/api/HttpExample". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpExample
     * 2. curl "{your host}/api/HttpExample?name=HTTP%20Query" SatellitesIn
     */
    @FunctionName("quasar")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "topsecret",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Se recibe peticion para ubicacion de los satellites");

        BasicConfigurator.configure();
        // Parse query parameter
        final String query = request.getQueryParameters().get("name");
        final String satelliteString = request.getBody().orElse(query);
        Gson gson = new Gson();
        SatellitesIn satellites = gson.fromJson(satelliteString, SatellitesIn.class);

        if (satelliteString == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Datos incompletos").build();
        } else {
        	LogicaQuasar logica = new LogicaQuasar();
        	ResultadoUbicacion resultado;
        	ResultadoSatellitesOut resultadoSatellite = new ResultadoSatellitesOut();
			try {
				resultado = logica.iniciarProcesoUbicacion(satellites);
				
	        	if(resultado.isExitoso()) {
	        		context.getLogger().info("|| pasoo : " + resultado.getMensaje() );
	        		Ubicacion ubicacion = new Ubicacion();
	        		ubicacion.setX(resultado.getPosicionX());ubicacion.setY(resultado.getPosicionY());
	        		resultadoSatellite.setMessage(resultado.getMensaje());
	        		resultadoSatellite.setPosition(ubicacion);
	        		return request.createResponseBuilder(HttpStatus.OK).body(resultadoSatellite).build();
	        	}else {
	        		context.getLogger().severe("Ocurrio un error inesperado");
	        		return request.createResponseBuilder(HttpStatus.NOT_FOUND).body(resultadoSatellite).build();
	        	}
			} catch (ExcepcionQasar e) {
				context.getLogger().severe(e.getMessage());
				return request.createResponseBuilder(HttpStatus.NOT_FOUND).body(resultadoSatellite).build();
			}
        	
            
        }
    }
}
