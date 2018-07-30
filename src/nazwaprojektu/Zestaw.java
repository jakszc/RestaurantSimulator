/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nazwaprojektu;

import java.io.Serializable;
/**
 *
 * @author Kubik
 */
public class Zestaw implements Serializable{
    private String nazwaZestawu;
    private double cenaZestawu;
    private long czasPrzygotowaniaZestawu;
    public Zestaw(String rozmiar, String nazwa, double cena, long czas){
        nazwaZestawu="";
        nazwaZestawu=nazwaZestawu.concat("3x "+rozmiar+" "+nazwa);
        cenaZestawu=2*cena;
        czasPrzygotowaniaZestawu=3*czas;
    }
    
    public String outDane(){
        String dane = "";
        dane=dane.concat("nazwa: "+nazwaZestawu+'\n');
        dane=dane.concat("cena: "+cenaZestawu+'\n');
        dane=dane.concat("czas Przygotowania: "+czasPrzygotowaniaZestawu+'\n');
        return dane;
    }

    public String getNazwaZestawu() {
        return nazwaZestawu;
    }

    public double getCenaZestawu() {
        return cenaZestawu;
    }

    public double getCzasPrzygotowaniaZestawu() {
        return czasPrzygotowaniaZestawu;
    }
}
