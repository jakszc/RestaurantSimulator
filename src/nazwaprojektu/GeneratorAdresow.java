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

public class GeneratorAdresow implements Serializable{
    private HashMap<String,Integer> wspolUlicY= new HashMap<>();
    private HashMap<String,Integer> wspolDomowX= new HashMap<>();
    private boolean[][] tablicaAdr= new boolean [4][29];
    private int ileWolnych, ilePosilkow, ilePojazdow;
    private Random r = new Random();
    private boolean[] tablicaPosilkow= new boolean[18];
    private boolean[] tablicaPojazdow= new boolean[900];
    private String[][] pixele= new String[666][1201];
    
    public GeneratorAdresow(){
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 28; j++) {
                tablicaAdr[i][j]=false;
            }
        }
        for (int j = 0; j < 28; j+=2) {
            tablicaAdr[0][j]=true;
        }
        for (int j = 1; j < 28; j+=2) {
            tablicaAdr[3][j]=true;
        }
        tablicaAdr[2][1]=true; tablicaAdr[2][3]=true; tablicaAdr[2][5]=true;
        tablicaAdr[3][0]=true; tablicaAdr[3][2]=true; tablicaAdr[3][4]=true;
        ileWolnych = 78;
        wspolUlicY.put("Zeromowa", 40);
        wspolUlicY.put("Jedynowa", 235);
        wspolUlicY.put("Dwojkowa", 430);
        wspolUlicY.put("Trojkowa", 625);
        wspolDomowX.put("0", 36);
        wspolDomowX.put("2", 103);
        wspolDomowX.put("4", 170);
        wspolDomowX.put("6", 350);
        wspolDomowX.put("8", 417);
        wspolDomowX.put("10", 484);
        wspolDomowX.put("12", 551);
        wspolDomowX.put("14", 618);
        wspolDomowX.put("16", 685);
        wspolDomowX.put("18", 752);
        wspolDomowX.put("20", 819);
        wspolDomowX.put("22", 998);
        wspolDomowX.put("24", 1065);
        wspolDomowX.put("26", 1132);
        for (int i = 0; i < 18; i++) {
            tablicaPosilkow[i]=false;
        }
        ilePosilkow=18;
        for (int i = 0; i < 900; i++) {
            tablicaPojazdow[i]=false;
        }
        ilePojazdow=900;
        for (int i = 0; i < 666; i++) {
            for (int j = 0; j < 1201; j++) {
                pixele[i][j]="";
            }
        }
    }
    
    public String przydzielAdres() throws Exception {
        if (ileWolnych == 0){
            throw new Wyjatek();
        }
        String adr = "";
        int liczba = r.nextInt(ileWolnych)+1;
        int pom=0, pomx=-1, pomy=-1;          
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 28; j++) {
                if (!tablicaAdr[i][j]) pom++;
                if(pom == liczba){
                    pomx = i;
                    pomy = j;
                    tablicaAdr[i][j]=true;
                    i=4; j=28;
                }
            }
        }
        switch(pomx){
            case 0: adr=adr.concat("Zeromowa ");
                break;
            case 1: adr=adr.concat("Jedynowa ");
                break;
            case 2: adr=adr.concat("Dwojkowa ");
                break;
            case 3: adr=adr.concat("Trojkowa ");
                break;
        }
        adr=adr.concat(Integer.toString(pomy));
        ileWolnych--;
        return adr;
    }
    
    public int zwrocX(String nrDomu){
        if(Integer.parseInt(nrDomu)%2 == 1){
            nrDomu=Integer.toString(Integer.parseInt(nrDomu)-1);
        }
        return wspolDomowX.get(nrDomu);
    }
    
    public int zwrocY(String nrUlicy){
        return wspolUlicY.get(nrUlicy);
    }

    public void setTablicaAdr(String adr) {
        int u=0, d;
        switch(adr.charAt(0)){
            case 'Z': u=0;
                break;
            case 'J': u=1;
                break;
            case 'D': u=2;
                break;
            case 'T': u=3;
                break;
        }
        d=Integer.parseInt(adr.substring(adr.indexOf(" ")+1));
        tablicaAdr[u][d]=false;
        ileWolnych++;
    }
    
    public int przydzielNrPosilku() throws Exception{
        if(ilePosilkow == 0) throw new Wyjatek();
        int pomNR=0, liczba, pom=0;
        liczba=r.nextInt(ilePosilkow)+1;
        for (int i = 0; i < 18; i++) {
            if(!tablicaPosilkow[i]) pom++;
            if(pom == liczba){
                pomNR=i;
                tablicaPosilkow[i]=true;
                break;
            }
        }
        ilePosilkow--;
        return pomNR;
    }
    
    public int przydzielNrPojazdu() throws Exception{
        if(ilePojazdow == 0) throw new Wyjatek();
        int pomNR=0, liczba, pom=0;
        liczba=r.nextInt(ilePojazdow)+1;
        for (int i = 0; i < 900; i++) {
            if(!tablicaPojazdow[i]) pom++;
            if(pom == liczba){
                pomNR=i+100;
                tablicaPojazdow[i]=true;
                break;
            }
        }
        ilePojazdow--;
        return pomNR;
    }

    public boolean sprawdzPixele(int x1, int y1, int x2, int y2) {
        if(x1 < 0) x1=0;
        if(y1 < 0) y1=0;
        if(x2 > 1200 ) x2=1200;
        if(y2 > 665 ) y2=665;
        for (int i = y1+1; i < y2; i++) {
            for (int j = x1+1; j < x2; j++) {
                if(!pixele[i][j].equals("")) {
                    return false;
                }
            }
        }
        return true;
    }

    public void zajmijPixele(int x1, int y1, int x2, int y2, String klucz){
        if(x1 < 0) x1=0;
        if(y1 < 0) y1=0;
        if(x2 > 1200 ) x2=1200;
        if(y2 > 665 ) y2=665;
        for (int i = y1+1; i < y2; i++) {
            for (int j = x1+1; j < x2; j++) {
                pixele[i][j]=klucz;
            }
        }
    }

    public void zwolnijPixele(int x1, int y1, int x2, int y2){
        if(x1 < 0) x1=0;
        if(y1 < 0) y1=0;
        if(x2 > 1200 ) x2=1200;
        if(y2 > 665 ) y2=665;
        for (int i = y1+1; i < y2; i++) {
            for (int j = x1+1; j < x2; j++) {
                pixele[i][j]="";
            }
        }
    }
    
    public String zwrocKlucz(int x, int y){
        if(x > 1200 || y > 665) return "";
        return pixele[y][x];
    }
    
    public void wyczyscPixele(){
        for (int i = 0; i < 666; i++) {
            for (int j = 0; j < 1201; j++) {
                pixele[i][j]="";
            }
        }
    }
}
