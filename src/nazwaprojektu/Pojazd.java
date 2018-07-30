/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nazwaprojektu;

import java.io.Serializable;
import java.util.*;
/**
 *
 * @author Kubik
 */
public class Pojazd implements Serializable{
    private double ladownosc;
    private int vMax;
    private String nrRejestracyjny;
    private int pojBaku;
    private int stanBaku;
    private String kategoria;
    
    public Pojazd(String nrRej,int i){
        Random r = new Random();
        nrRejestracyjny = nrRej;
        
        if (i == 1) {
            kategoria="Wielbłąd";
            pojBaku=3600;
            stanBaku=3600;
            vMax=3;
            ladownosc=100;
            
        }
        else{
            kategoria="Słoń";
            pojBaku=10000;
            stanBaku=10000;
            vMax=1;
            ladownosc=500;
        }
    }
    
    public String outDane(){
        String dane="";
        dane=dane.concat(kategoria+"   "+nrRejestracyjny+'\n');
        dane=dane.concat("vMax: "+vMax*10+" km/h"+"      ladownosc: "+ladownosc+" kg\n");
        dane=dane.concat("paliwo: "+stanBaku+"/"+pojBaku+" px");
        return dane;
    }

    public double getLadownosc() {
        return ladownosc;
    }

    public int getPojBaku() {
        return pojBaku;
    }

    public int getStanBaku() {
        return stanBaku;
    }

    public void setStanBaku(int stanBaku) {
        this.stanBaku = stanBaku;
    }

    public int getvMax() {
        return vMax;
    }

    public String getNrRejestracyjny() {
        return nrRejestracyjny;
    }
    
}
