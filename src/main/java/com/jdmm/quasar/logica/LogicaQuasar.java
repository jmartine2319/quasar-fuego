package com.jdmm.quasar.logica;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jdmm.quasar.dto.ResultadoUbicacion;
import com.jdmm.quasar.dto.SatellitesIn;
import com.jdmm.quasar.util.ExcepcionQasar;
import com.jdmm.quasar.util.RedisClient;
import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver;
import com.lemmingapex.trilateration.TrilaterationFunction;

public class LogicaQuasar {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LogicaQuasar.class);
	
	public ResultadoUbicacion iniciarProcesoUbicacion(SatellitesIn satellites) throws ExcepcionQasar {
		ResultadoUbicacion resultado = new ResultadoUbicacion();
		resultado.setExitoso(false);
		List<LinkedHashMap<Integer,String>> listaMensajes = new ArrayList<>();
		if(satellites.getSatellites().size()>3) {
			return resultado;
		}
		for(int i=0;i<satellites.getSatellites().size();i++) {
			LinkedHashMap<Integer,String> mensajes=limpiarMensaje(satellites.getSatellites().get(i).getMessage());
			listaMensajes.add(mensajes);
			//listaDistancias[i]=satellites.getSatellites().get(i).getDistance();
		}
		ResultadoUbicacion mensaje = fusionarMensajes(listaMensajes);
		ResultadoUbicacion distancia = calcularUbicacion(satellites);
		if(mensaje.isExitoso() && distancia.isExitoso()) {
			resultado.setExitoso(true);
			resultado.setPosicionX(distancia.getPosicionX());
			resultado.setPosicionY(distancia.getPosicionY());
			resultado.setMensaje(mensaje.getMensaje());
		}else {
			if(!mensaje.isExitoso()) {
				throw new ExcepcionQasar("Ocurrio un error al descifrar los mensajes");
			}
			if(!distancia.isExitoso()) {
				throw new ExcepcionQasar("Ocurrio un error al calcular la ubicacion de los satellites");
			}
		}
		return resultado;
	}

	/**
	 * Metodo para calcular la ubicacion usando la distancia respecto a los otros satelites
	 */
	private ResultadoUbicacion calcularUbicacion(SatellitesIn satellites) {
		ResultadoUbicacion resultado = new ResultadoUbicacion();
		resultado.setExitoso(true);
		try {
			double[] listaDistancias = new double[3];
			double[] satellite1 = obtenerUbicacion(satellites.getSatellites().get(0).getName());
			double[] satellite2 = obtenerUbicacion(satellites.getSatellites().get(1).getName());
			double[] satellite3 = obtenerUbicacion(satellites.getSatellites().get(2).getName());
			for(int i=0;i<satellites.getSatellites().size();i++) {
				listaDistancias[i]=satellites.getSatellites().get(i).getDistance();
			}
			if(satellite1.length==0 && satellite2.length==0 && satellite3.length==0 ) {
				System.out.println("no se pudo obtener las propiedades");
				resultado.setExitoso(false);
				return resultado;
			}
			double[][] posiciones = new double[][] { satellite1, satellite2, satellite3 };
	
			NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(posiciones, listaDistancias), new LevenbergMarquardtOptimizer());
			Optimum optimum = solver.solve();
	
			double[] centroid = optimum.getPoint().toArray();
	
			resultado.setPosicionX(centroid[0]);
			resultado.setPosicionY(centroid[1]);
		}catch(Exception e) {
			LOGGER.error("Ocurrio un error en calcularUbicacion ",e);
			resultado.setExitoso(false);
		}
		return resultado;
	}
	
	/**
	 * Metodo para obtener la ubicacion
	 * @param nombreSatellite
	 * @return
	 */
	private double[] obtenerUbicacion(String nombreSatellite) {
		RedisClient redis = new RedisClient();
		String propiedad = redis.consultarPropiedad(nombreSatellite);
		String[] ubicacionXY=propiedad.split(",");
		double[] ubicacionSatellite = new double[2];
		if(propiedad.equals("") || ubicacionXY.length<2) {
			return ubicacionSatellite;
		}
		ubicacionSatellite[0]=Double.parseDouble(ubicacionXY[0]);
		ubicacionSatellite[0]=Double.parseDouble(ubicacionXY[1]);
		return ubicacionSatellite;
	}
	
	/**
	 * Metodo para verificar que no llegue datos que no sean basura
	 * @param mensajes
	 * @return
	 */
	private static LinkedHashMap<Integer,String> limpiarMensaje(List<String> mensajes) {
	
		LinkedHashMap<Integer,String> mensajeDes = new LinkedHashMap<Integer,String>();
		for(int i=0;i<mensajes.size();i++) {
			
			String pattern = "[^A-Za-z0-9]";
			Pattern r = Pattern.compile(pattern);
			Matcher m = r.matcher(mensajes.get(i));
		    if (m.find( )) {
		    	mensajeDes.put(i,"");
		    }else {
		    	mensajeDes.put(i, mensajes.get(i));
		    }
			
		}
		return mensajeDes;
	}
	
	/**
	 * Metodo que fusiona los mensajes recibidos por los satellites
	 * @param mensajes
	 * @return
	 */
	private ResultadoUbicacion fusionarMensajes(List<LinkedHashMap<Integer,String>> mensajes) {
		ResultadoUbicacion mensajeOut = new ResultadoUbicacion();
		mensajeOut.setExitoso(true);
		LinkedHashMap<Integer,String> mensajeDescifrado = new LinkedHashMap<Integer,String>();
		int tamanoArreglo=0;
		for(int i =0;i<mensajes.size();i++) {
			tamanoArreglo = mensajes.size();
			if(i>0 && tamanoArreglo>0) {
				if(tamanoArreglo>mensajes.size() || tamanoArreglo<mensajes.size()) {
					mensajeOut.setExitoso(false);
					return mensajeOut;
				}
			}
			LinkedHashMap<Integer,String> mensaje= mensajes.get(i);
			if(i==0) {
				mensaje.forEach((key,value) -> {
					mensajeDescifrado.put(key, value);
				});
			}else {
				mensaje.forEach((key,value) -> {
					if(value.length()>0) {
						mensajeDescifrado.put(key, value);
					}
					
				});
			}
		}
		StringBuffer mensajeDes = new StringBuffer();
		mensajeDescifrado.forEach((key,value) -> {
			
			if(key==0) {
				mensajeDes.append(value);
			}else {
				mensajeDes.append(" ");
				mensajeDes.append(value);
			}
			
			
		});
		mensajeOut.setMensaje(mensajeDes.toString());
		return mensajeOut;
	}
}
