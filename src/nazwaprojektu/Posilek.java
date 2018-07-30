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
public class Posilek implements Serializable{
    private String nazwa;
    private List<String> skladniki = new ArrayList<>();
    private double cena;
    private String kategoria;
    private String rozmiar;
    private long czasPrzygotowania;
    
    public Posilek(int numer){
        Random r = new Random();
        int liczba;
        String[] kat = {"Na Szybko","Szlachta","Plebs"};
        String[] naz = {"Kebab","Frytki","Bolognese","Jajecznica","Burger","Pałki z kurczaka",
            "Zap. ziemniaczki z królikiem","Danie Krola","Ratatouille","Morska pieczeń","Żabie udka po francusku","Jarosław z wyborcami",
            "Pyry z gzikiem","Schaboszczak","Pomidorowa","Zupa szczawiowa","Pesto","Barszcz z uszkami"};
        int[] cen = {10,5,10,10,10,10,35,50,40,45,35,55,15,15,10,10,10,10};
        int[] czas = {250,250,250,250,250,250,1000,1000,1000,1000,1000,1000,500,500,500,500,500,500};
        String[][] sklad = {{"Bula","Surowa","Miecho","Sosy"},{"Pyry","Sol","Keczup"},{"Makaron","Sos Pomidorowy","Mielone"},{"Jajka","Cebula","Boczek"},{"Buła","Wołowina","Sałata","sos BBQ"},{"Nóżki z kurczaka","Pyry","Tłuszcz"},
            {"Pol krolika","Pyry","Zielone","Sos HM"},{"Niespodzianka"},{"Warzywa","Pelno warzyw"},{"Pstrąg","Wodorosty","Pyry z ogniska"},{"Żabie udka","Natka pietruszki","Sok z cytryny"},{"Kaczka","buraczki"},
            {"Pyry","Gzik"},{"Kotlet","Ser"},{"Rosol","Tajemny skladnik zamieniajacy rosol w pomidorowa"},{"Wieprzowina","Szczaw","Jajka"},{"Bazylia","Migdały","Orzechy","Parmezan"},{"Grzyby","Buraki"}};
        kategoria = kat[(int)Math.floor(numer/6)];
        nazwa = naz[numer];
        cena = cen[numer];
        czasPrzygotowania = czas[numer];
        skladniki.addAll(Arrays.asList(sklad[numer]));
        liczba = r.nextInt(2);
        if (liczba == 0) {
            rozmiar = "Mala Porcja";
            cena = cena * 0.75;
        }
        else rozmiar = "Duza Porcja";
    }
    
    public String outDane(){
        String dane = "";
        dane=dane.concat("nazwa: "+nazwa+'\n');
        dane=dane.concat("cena: "+cena+'\n');
        dane=dane.concat("kategoria: "+kategoria+'\n');
        dane=dane.concat("rozmiar: "+rozmiar+'\n');
        dane=dane.concat("czas Przygotowania: "+czasPrzygotowania+'\n');
        return dane;
    }

    public String getNazwa() {
        return nazwa;
    }

    public double getCena() {
        return cena;
    }

    public String getRozmiar() {
        return rozmiar;
    }

    public long getCzasPrzygotowania() {
        return czasPrzygotowania;
    }
    
}
