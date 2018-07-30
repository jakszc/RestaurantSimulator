/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nazwaprojektu;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import static nazwaprojektu.Restauracja.kolejkaZamowien;
import static nazwaprojektu.NazwaProjektu.lockKolejkaZamowien;
import static nazwaprojektu.NazwaProjektu.lockPixele;
/**
 *
 * @author Kubik
 */
public abstract class Klient extends Czlowiek implements Serializable{
    protected String numerTel;
    protected String adres;
    private Random r = new Random();
    private int liczba;
    private int zamowienOczekujacych;
    private String typ;
    
    public Klient(){
        numerTel="";
        numerTel=numerTel.concat(Integer.toString(r.nextInt(9)+1));
        for (int i = 0; i < 8; i++) {
            liczba= r.nextInt(10);
            numerTel=numerTel.concat(Integer.toString(liczba));
        }
        zamowienOczekujacych=0;
    }
    
    @Override
    public void run(){
        while(run){
            this.zlozZamowienie();
            try{
                TimeUnit.MILLISECONDS.sleep(r.nextInt(5000)+10000);
            }
            catch(InterruptedException e){
                
            }
        }
    }

    public String outDane(){
        String dane = "";
        dane=dane.concat("nazwa: "+nazwa+'\n');
        dane=dane.concat("kom: "+numerTel+'\n');
        dane=dane.concat("adres: "+adres+'\n');
        return dane;
    }
    
    public int getNrDomu(){
        return Integer.parseInt(adres.substring(adres.indexOf(" ")+1));
    }
    
    public void zlozZamowienie(){
        int nrD=Integer.parseInt(adres.substring(adres.indexOf(" ")+1));
        try {
            Zamowienie zam = new Zamowienie(this.getPozycjaX(),this.getPozycjaY(),adres,nrD,1);
            zamowienOczekujacych++;
            lockKolejkaZamowien.lock();
            try{
                kolejkaZamowien.add(zam);
            }finally{
                lockKolejkaZamowien.unlock();
            }
            
            
        } catch (Exception ex) {
            Logger.getLogger(Klient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getAdres() {
        return adres;
    }
    
    public void zaznaczNaMapiePixeli(GeneratorAdresow gen){
        lockPixele.lock();
        try{
            if(Integer.parseInt(adres.substring(adres.indexOf(" ")+1))%2 == 0){
                gen.zajmijPixele(this.getPozycjaX()-30, this.getPozycjaY()-100, this.getPozycjaX()+30, this.getPozycjaY()-40, adres);
            }
            else {
                gen.zajmijPixele(this.getPozycjaX()-30, this.getPozycjaY()+40, this.getPozycjaX()+30, this.getPozycjaY()+100, adres);
            }
        }finally{ lockPixele.unlock();}
    }
    
    public void zmniejszIloscZamowien(){
        zamowienOczekujacych--;
    }
    
    public int getZamowienOczekujacych(){
        return zamowienOczekujacych;
    }
    
    public void zwiekszIloscZamowien(){
        zamowienOczekujacych++;
    }

    public String getTyp() {
        return typ;
    }

    public void setTyp(String typ) {
        this.typ = typ;
    }
    
    public void ustawIkone(String rodzaj){
        if(rodzaj.equals("Firmowy")) ikona=DolaczObrazek("/img/firmowy.png");
        else if(rodzaj.equals("Okazjonalny")) ikona=DolaczObrazek("/img/okazjonalny.png");
        else if(rodzaj.equals("Staly")) ikona=DolaczObrazek("/img/staly.png");
    }
    
}
