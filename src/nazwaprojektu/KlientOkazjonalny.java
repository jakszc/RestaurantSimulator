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
public class KlientOkazjonalny extends Klient implements Serializable{
    public KlientOkazjonalny(String adr, GeneratorAdresow gen){
        this.setTyp("Okazjonalny");
        ikona=DolaczObrazek("/img/okazjonalny.png");
        adres=adr;
        int pomX,pomY;
        pomY=gen.zwrocY(adres.substring(0, 8));
        pomX=gen.zwrocX(adres.substring(adres.indexOf(" ")+1));
        this.setPozycja(pomX, pomY);
        this.zaznaczNaMapiePixeli(gen);
    }
    
    @Override
    public String outDane(){
        String dane;
        dane=super.outDane();
        dane=dane.concat("Zamowien aktualnie: "+Integer.toString(this.getZamowienOczekujacych()));
        return dane;
    }
}
