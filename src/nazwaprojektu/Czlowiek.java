/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nazwaprojektu;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import javax.imageio.ImageIO;
/**
 *
 * @author Kubik
 */
public abstract class Czlowiek extends Thread implements Serializable{
    protected String nazwa;
    transient protected BufferedImage ikona;
    protected boolean run;
    private int pozycjaX;
    private int pozycjaY;
    
    public Czlowiek(){
        run=true;
        Random r = new Random();
        int liczba1, liczba2;
        String[] imiona = {"Anna","Bela","Cecylia","Dominik","Eustachy","Fidel","Grazyna","Hubert","Ira","Jozef",
                            "Klaus","Lenin","Marian","Nisza","Oberyn","Patryk","Quinn","Renata","Stach","Teodor",
                            "Umbert","Viktoria","Wania","Xawery","Yeti","Zenon"};
        String[] nazwiska = {"Amber","Baran","Cyjan","Domen","Euros","Fann","Gwik","Hompcy","Impik","Josza",
                            "Klucha","Lama","Magik","Nowel","Obam","Piwda","Qurczak","Reiss","Supit","Trach",
                            "Ulon","Venno","Wikun","Xumba","Yuta","Zawro"};
        liczba1 = r.nextInt(26);
        liczba2 = r.nextInt(26);
        nazwa = imiona[liczba1]+" "+nazwiska[liczba2];
    }
    
    BufferedImage DolaczObrazek(String nazwaPNG){
        BufferedImage img = null;
        try {
                img =ImageIO.read(getClass().getResource(nazwaPNG));
        } catch (IOException e) {
                System.err.println("Blad odczytu obrazka");
        }
        return img;
    }

    public void setPozycja(int pozycjaX, int pozycjaY) {
        this.pozycjaX = pozycjaX;
        this.pozycjaY = pozycjaY;
    }

    public int getPozycjaX() {
        return pozycjaX;
    }

    public int getPozycjaY() {
        return pozycjaY;
    }

    public BufferedImage getIkona() {
        return ikona;
    }

    public void setRun(boolean run) {
        this.run = run;
    }
    
    
}
