/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nazwaprojektu;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import static nazwaprojektu.NazwaProjektu.lockKolejkaZamowien;
import static nazwaprojektu.Restauracja.kolejkaZamowien;
/**
 *
 * @author Kubik
 */
public class KlientStaly extends Klient implements Serializable{
    private double punktyLojalnosciowe;
    private double znizka;
    
    public KlientStaly(String adr, GeneratorAdresow gen){
        this.setTyp("Staly");
        ikona=DolaczObrazek("/img/staly.png");
        adres=adr;
        int pomX,pomY;
        pomY=gen.zwrocY(adres.substring(0, 8));
        pomX=gen.zwrocX(adres.substring(adres.indexOf(" ")+1));
        this.setPozycja(pomX, pomY);
        this.zaznaczNaMapiePixeli(gen);
        punktyLojalnosciowe=0;
        znizka=0.8;
    }
    
    @Override
    public String outDane(){
        String dane;
        dane=super.outDane();
        dane=dane.concat("punkty: "+punktyLojalnosciowe+'\n');
        dane=dane.concat("znizka: "+(int)(znizka*100)+"%"+'\n');
        dane=dane.concat("Zamowien aktualnie: "+Integer.toString(this.getZamowienOczekujacych()));
        return dane;
    }
    
    @Override
    public void zlozZamowienie(){
        int nrD=Integer.parseInt(adres.substring(adres.indexOf(" ")+1));
        try {
            Zamowienie zam = new Zamowienie(this.getPozycjaX(),this.getPozycjaY(),adres,nrD,znizka);
            this.zwiekszIloscZamowien();
            this.dodajPunkty(zam.getCena());
            if(punktyLojalnosciowe > 500){
                zam.uwzglednijPunkty(100);
                this.odejmijPunkty();
            }
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
    
    public void dodajPunkty(double pktLoj){
        this.punktyLojalnosciowe+=pktLoj;
    }
    
    public void odejmijPunkty(){
        this.punktyLojalnosciowe-=500;
    }
}
