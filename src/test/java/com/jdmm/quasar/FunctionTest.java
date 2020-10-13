package com.jdmm.quasar;

import com.jdmm.quasar.dto.ResultadoUbicacion;
import com.jdmm.quasar.dto.SatelliteIn;
import com.jdmm.quasar.dto.SatellitesIn;
import com.jdmm.quasar.logica.LogicaQuasar;
import com.microsoft.azure.functions.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.*;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


/**
 * Unit test for Function class.
 */
public class FunctionTest {
    /**
     * Unit test for HttpTriggerJava method.
     */
    @Test
    public void testHttpTriggerJava() throws Exception {
        // Setup
    	LogicaQuasar logicaQuasar = new LogicaQuasar();
    	SatellitesIn satellites = new SatellitesIn();
    	SatelliteIn satellite = new SatelliteIn();
    	satellite.setDistance(150.0);
    	List<String> listaMensajes = new ArrayList<>();
    	listaMensajes.add("hola");
    	listaMensajes.add("");
    	listaMensajes.add("julian");
    	satellite.setName("skywalker");
    	satellite.setMessage(listaMensajes);
    	SatelliteIn satellite2 = new SatelliteIn();
    	satellite2.setDistance(115.0);
    	List<String> listaMensajes2 = new ArrayList<>();
    	listaMensajes2.add("*");
    	listaMensajes2.add("soy");
    	listaMensajes2.add("julian");
    	satellite2.setName("sato");
    	satellite2.setMessage(listaMensajes2);
    	SatelliteIn satellite3 = new SatelliteIn();
    	satellite3.setDistance(115.0);
    	List<String> listaMensajes3 = new ArrayList<>();
    	listaMensajes3.add("");
    	listaMensajes3.add("soy");
    	listaMensajes3.add("julian");
    	satellite3.setName("sato");
    	satellite3.setMessage(listaMensajes3);
    	List<SatelliteIn> listaSatellite = new ArrayList<>();
    	listaSatellite.add(satellite);
    	listaSatellite.add(satellite2);
    	//listaSatellite.add(satellite3);
    	satellites.setSatellites(listaSatellite);
    	ResultadoUbicacion resultado = logicaQuasar.iniciarProcesoUbicacion(satellites);
    	

        // Verify
        assertEquals(resultado.getMensaje(),"hola soy julian");
    }
}
