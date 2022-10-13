package com.vitalsoftware.taxifast;

import java.io.Serializable;

/**
 * Created by titin on 11/21/16.
 */

public class Pedido implements Serializable {
    public String direccion, referencia;
    public double latitud, longitud;
    public int id;
}