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
public class KlientFirmowy extends Klient implements Serializable{
    private String nrKonta;
    private String nrREGON;
    private String adresSpamu;
    
    public KlientFirmowy(String adr, GeneratorAdresow gen){
        this.setTyp("Firmowy");
        ikona=DolaczObrazek("/img/firmowy.png");
        int poz = nazwa.indexOf(" ");
        nazwa = nazwa.substring(0, 2)+nazwa.substring(poz+1, poz+3)+" S.A.";
        adres=adr;
        int pomX,pomY;
        pomY=gen.zwrocY(adres.substring(0, 8));
        pomX=gen.zwrocX(adres.substring(adres.indexOf(" ")+1));
        this.setPozycja(pomX, pomY);
        this.zaznaczNaMapiePixeli(gen);
        Random r = new Random();
        int liczba;
        nrKonta="";
        nrREGON="";
        adresSpamu="";
        for (int i = 0; i < 26; i++) {
            liczba=r.nextInt(10);
            nrKonta=nrKonta.concat(Integer.toString(liczba));
        }
        nrKonta=nrKonta.substring(0, 2)+" "+nrKonta.substring(2, 6)+" "+nrKonta.substring(6, 10)+" "+nrKonta.substring(10, 14)
                                        +" "+nrKonta.substring(14, 18)+" "+nrKonta.substring(18, 22)+" "+nrKonta.substring(22, 26);
        for (int i = 0; i < 9; i++) {
            liczba=r.nextInt(10);
            nrREGON=nrREGON.concat(Integer.toString(liczba));
        }
        adresSpamu=(nazwa.toLowerCase().substring(0, 4))+"@aoe.hd";
        
    }
    
    @Override
    public String outDane(){
        String dane;
        dane=super.outDane();
        dane=dane.concat("nr konta: "+nrKonta+'\n');
        dane=dane.concat("nr REGON: "+nrREGON+'\n');
        dane=dane.concat("adres korespondencji: "+adresSpamu+'\n');
        dane=dane.concat("Zamowien aktualnie: "+Integer.toString(this.getZamowienOczekujacych()));
        return dane;
    }
}
