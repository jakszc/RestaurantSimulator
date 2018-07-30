/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nazwaprojektu;

import java.io.Serializable;
import java.util.*;
import static nazwaprojektu.Restauracja.menu;
import static nazwaprojektu.Restauracja.menuZestawow;

/**
 *
 * @author Kubik
 */
public class Zamowienie implements Serializable{
    private List<Posilek> zbiorPosilkow = new ArrayList<>();
    private List<Zestaw> zbiorZestawow = new ArrayList<>();
    private long godzinaZamowienia;
    private long czasPrzygotowania;
    private long godzinaGotowosci;
    private double cena;
    private double kosztDostawy;
    private int adresX;
    private int adresY;
    private String adres;
    private int odleglosc;
    private int nrDomu;
    
    public Zamowienie(int aX, int aY, String adr, int nrD, double znizka) throws Exception{
        if(menu.isEmpty()) throw new Wyjatek();
        cena=0;
        kosztDostawy=0;
        czasPrzygotowania=0;
        adresX=aX;
        adresY=aY;
        adres=adr;
        nrDomu=nrD;
        Random r = new Random();
        int liczba;
        liczba=r.nextInt(menu.size())+1;
        for (int i = 0; i < liczba; i++) {
            if(r.nextInt(2) == 0){
                zbiorPosilkow.add(menu.get(r.nextInt(menu.size())));
                cena+=zbiorPosilkow.get(zbiorPosilkow.size()-1).getCena();
                czasPrzygotowania+=zbiorPosilkow.get(zbiorPosilkow.size()-1).getCzasPrzygotowania();
            }
            else {
                zbiorZestawow.add(menuZestawow.get(r.nextInt(menuZestawow.size())));
                cena+=zbiorZestawow.get(zbiorZestawow.size()-1).getCenaZestawu();
                czasPrzygotowania+=zbiorZestawow.get(zbiorZestawow.size()-1).getCzasPrzygotowaniaZestawu();
            }
        }
        cena=cena*znizka;
        odleglosc=Math.abs(36-adresX)+Math.abs(625-adresY);
        if(odleglosc > 800) kosztDostawy=Math.floor((odleglosc-800)/60);
        if(cena > 100) kosztDostawy=0;
        godzinaZamowienia=new Date().getTime();
        godzinaGotowosci=godzinaZamowienia+czasPrzygotowania;
       // System.out.println("Klient: "+new Date(godzinaZamowienia)+" "+new Date(godzinaGotowosci)+" "+odleglosc);
    }
    
    public long getGodzinaZamowienia() {
        return godzinaZamowienia;
    }

    public long getGodzinaGotowosci() {
        return godzinaGotowosci;
    }

    public double getCena() {
        return cena;
    }

    public double getKosztDostawy() {
        return kosztDostawy;
    }

    public int getAdresX() {
        return adresX;
    }

    public int getAdresY() {
        return adresY;
    }

    public int getOdleglosc() {
        return odleglosc;
    }

    public int getNrDomu() {
        return nrDomu;
    }
    
    public void uwzglednijPunkty(double pktLoj){
        this.cena-=pktLoj;
        if(cena<0) cena=0;
    }

    public String getAdres() {
        return adres;
    }
    
}
