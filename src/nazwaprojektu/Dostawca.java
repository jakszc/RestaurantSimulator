/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nazwaprojektu;

import java.util.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.nashorn.internal.runtime.regexp.joni.EncodingHelper;
import static nazwaprojektu.NazwaProjektu.restauracja;
import static nazwaprojektu.Restauracja.klienci;
import static nazwaprojektu.Restauracja.garaz;
import static nazwaprojektu.Restauracja.kolejkaZamowien;
import static nazwaprojektu.NazwaProjektu.lockKolejkaZamowien;
import static nazwaprojektu.NazwaProjektu.lockGaraz;
import static nazwaprojektu.NazwaProjektu.lockPixele;

/**
 *
 * @author Kubik
 */
public class Dostawca extends Czlowiek implements Serializable{
    private String pesel;
    private int[][] harmonogram = new int[7][2];
    private List<String> kategoriePrawka = new ArrayList<>();
    private int celPodrozyX;
    private int celPodrozyY;
    private int nrDomu;
    private Queue<Zamowienie> bagaznik = new LinkedList<>();
    transient private BufferedImage ikonaPrawo;
    transient private BufferedImage ikonaGora;
    transient private BufferedImage ikonaLewo;
    transient private BufferedImage ikonaDol;
    private String nrRej;
    private boolean wTrasie;
    private boolean naglyPowrot;
    private String kierunekJazdy;
    private long czekaj;
    private int drogaDoPrzebycia;
    private double wartoscZamowien;
    private String typ;
    
    public Dostawca(GeneratorAdresow gen){
        Random r = new Random();
        int liczba;
        pesel="";
        for (int i = 0; i < 11; i++) {
            liczba = r.nextInt(10);
            pesel = pesel.concat(Integer.toString(liczba));
        }
        
        liczba = r.nextInt(7);
        harmonogram[liczba][0]=-1;
        harmonogram[liczba][1]=-1;
        for(int i = 0; i < 7; i++){
            if(harmonogram[i][0]== -1) continue;
            harmonogram[i][0] = r.nextInt(12)+1;
            harmonogram[i][1] = harmonogram[i][0] + 12;
        }
        nrRej = "AOE-X";
        try {
            nrRej = nrRej.concat(Integer.toString(gen.przydzielNrPojazdu()));
        } catch (Exception ex) {
            System.err.println("Nie mozna przydzielic pojazdu");
        }
        kategoriePrawka.add("Wielbłąd");
        if( r.nextInt(2) == 1){
            kategoriePrawka.add("Słoń");
        }
        Pojazd p;
        if(kategoriePrawka.size() == 1) {
            p= new Pojazd(nrRej,1);
            this.setTyp("Wielbłąd");
            ikona=DolaczObrazek("/img/rumak_prawo.png");
            ikonaPrawo=DolaczObrazek("/img/rumak_prawo.png");
            ikonaGora=DolaczObrazek("/img/rumak_gora.png");
            ikonaLewo=DolaczObrazek("/img/rumak_lewo.png");
            ikonaDol=DolaczObrazek("/img/rumak_dol.png");
        }
        else {
            p= new Pojazd(nrRej,2);
            this.setTyp("Słoń");
            ikona=DolaczObrazek("/img/slon_prawo.png");
            ikonaPrawo=DolaczObrazek("/img/slon_prawo.png");
            ikonaGora=DolaczObrazek("/img/slon_gora.png");
            ikonaLewo=DolaczObrazek("/img/slon_lewo.png");
            ikonaDol=DolaczObrazek("/img/slon_dol.png");
        }
        lockGaraz.lock();
        try{
            garaz.put(nrRej,p);
        }finally{
            lockGaraz.unlock();
        }
        wTrasie=false;
        naglyPowrot=false;
        this.setPozycja(36,625);
        kierunekJazdy="prawo";
    }
    
    @Override
    public void run() {
        int pomOdl, pomMaxOdl;
        long pomGot;
        double pomCena, pomMaxCena;
        int pomX, pomY, pomZamX, pomZamY;
        int pozycjaX, pozycjaY, crossX;
        boolean temp;
        while(run){
            czekaj=0;
            if(!wTrasie){
                //<editor-fold defaultstate="collapsed" desc="Implementacja pobierania Zamowien">
                wartoscZamowien=0;
                drogaDoPrzebycia=0;
                pomX=36;
                pomY=625;
                lockGaraz.lock();
                try{
                    pomMaxCena=garaz.get(nrRej).getLadownosc();
                    pomMaxOdl=garaz.get(nrRej).getPojBaku();
                }finally{
                    lockGaraz.unlock();
                }
                lockKolejkaZamowien.lock();
                try{
                    if(kolejkaZamowien.isEmpty()){
                        czekaj=1000;
                    }
                    else{
                        for (int i = 0; i < kolejkaZamowien.size(); i++) {
                            //// ZCZYTANIE WARTOSCI Z ZAMOWIENIA
                            pomGot=kolejkaZamowien.get(i).getGodzinaGotowosci();
                            pomCena=kolejkaZamowien.get(i).getCena();
                            pomZamX=kolejkaZamowien.get(i).getAdresX();
                            pomZamY=kolejkaZamowien.get(i).getAdresY();
                            pomOdl=this.policzOdleglosc(pomX, pomY, pomZamX, pomZamY);
                            
                            // JEZELI ZAMOWIENIE JEST GOTOWE i SPELNIA MOZLIWOSCI DOSTAWCY, TEN DODAJE JE DO BAGAZNIKA i USUWA Z KOLEJKI ZAMOWIEN
                            if(pomGot < new Date().getTime() && (wartoscZamowien+pomCena) <= pomMaxCena && (drogaDoPrzebycia+pomOdl) < pomMaxOdl){
                                bagaznik.add(kolejkaZamowien.get(i));
                                // System.out.println("Dostawca: "+new Date(pomGot)+" "+pomCena+" "+pomOdl);
                                kolejkaZamowien.remove(i);
                                i--;
                                wartoscZamowien+=pomCena;
                                drogaDoPrzebycia+=pomOdl;
                                pomX=pomZamX;
                                pomY=pomZamY;
                            }
                        }
                        // JEZELI ZADNE ZAMOWIENIE NIE SPELNIA MOZLIWOSCI DOSTAWCY, TEN BIERZE JEDNO Z GOTOWYCH - O ILE SA
                        // (bez wzgledu na cene(przymus) i na pojemnosc baku(pelny bak zawsze starcza na minimum jedno zamowienie))
                        if(bagaznik.isEmpty()){
                            for (int i = 0; i < kolejkaZamowien.size(); i++) {
                                pomGot=kolejkaZamowien.get(i).getGodzinaGotowosci();
                                if(pomGot < new Date().getTime()){
                                    bagaznik.add(kolejkaZamowien.get(i));
                                    wartoscZamowien+=kolejkaZamowien.get(i).getCena();
                                    drogaDoPrzebycia+=kolejkaZamowien.get(i).getOdleglosc();
                                    kolejkaZamowien.remove(i);
                                    break;
                                }
                            }
                        }
                        if(!bagaznik.isEmpty()){
                            celPodrozyX=bagaznik.peek().getAdresX();
                            celPodrozyY=bagaznik.peek().getAdresY();
                            nrDomu=bagaznik.peek().getNrDomu();
                            temp=false;
                            while(!temp){
                                lockPixele.lock();
                                try{
                                   temp=restauracja.getGenerator().sprawdzPixele(35, 625, 38, 665);
                                   if(temp) restauracja.getGenerator().zajmijPixele(0, 625, 37, 665, pesel);
                                }finally {lockPixele.unlock();}
                                try{
                                    TimeUnit.MILLISECONDS.sleep(30);
                                }
                                catch(InterruptedException e){}
                            }
                            garaz.get(nrRej).setStanBaku(garaz.get(nrRej).getPojBaku());
                            this.setPozycja(36, 625);
                            wTrasie=true;
                            naglyPowrot=false;
                            kierunekJazdy="prawo";
                            ikona=ikonaPrawo;
                        }
                    }
                }finally{
                    lockKolejkaZamowien.unlock();
                }
//</editor-fold>
            }
            else if(wTrasie){
                //<editor-fold defaultstate="collapsed" desc="Implementacja dowozu zamowien">
                // USTAWIENIE CELU
                if(!bagaznik.isEmpty() && !naglyPowrot){
                    temp=false;
                    while(!temp){
                        try {
                            celPodrozyX = bagaznik.peek().getAdresX();
                            celPodrozyY = bagaznik.peek().getAdresY();
                            nrDomu = bagaznik.peek().getNrDomu();
                            temp=true;
                        } catch (Exception e) {
                            bagaznik.poll();
                        }
                    }
                }
                // DOJAZD DO CELU
                pozycjaX=this.getPozycjaX();
                pozycjaY=this.getPozycjaY();
                if(naglyPowrot || bagaznik.isEmpty()){
                    celPodrozyX=1;
                    celPodrozyY=625;
                }
                if(pozycjaY != celPodrozyY && Math.abs(pozycjaY-celPodrozyY) > 40){    // JEST NA INNEJ ULICY
                    crossX=this.najblizszeSkrzyzowanie(pozycjaX);
                    if(crossX-pozycjaX > 0 && Math.abs(crossX-pozycjaX) > 60){    // najblizsze skrzyzowanie z prawej
                        if(kierunekJazdy.equals("lewo")) {
                            while(!this.zawroc()){
                                    try{TimeUnit.MILLISECONDS.sleep(100);}
                                    catch(InterruptedException e){}
                                }
                        }
                        if(this.drogaWolna()) pozycjaX++;
                    }
                    else if(crossX-pozycjaX < 0 && Math.abs(crossX-pozycjaX) > 60){   // najblizsze skrzyzowanie z lewej
                        if(kierunekJazdy.equals("prawo")) {
                            while(!this.zawroc()){
                                    try{TimeUnit.MILLISECONDS.sleep(100);}
                                    catch(InterruptedException e){}
                                }
                        }
                        if(this.drogaWolna()) pozycjaX--;
                    }
                    else if(Math.abs(crossX-pozycjaX) <= 60){ // DOJECHAL DO GRANICY SKRZYZOWANIA NA KTORYM MUSI SKRECIC =jazda w pionie
                        if(pozycjaY-celPodrozyY > 0){ // cel jest u gory
                            if(kierunekJazdy.equals("prawo")) {
                                while(!this.skrecWLewo()){      // kierunek= gora
                                    try{TimeUnit.MILLISECONDS.sleep(100);}
                                    catch(InterruptedException e){}
                                }
                                pozycjaX=this.getPozycjaX();
                                pozycjaY=this.getPozycjaY();
                            }
                            else if(kierunekJazdy.equals("lewo")) {
                                while(!this.skrecWPrawo()){     // kierunek= gora
                                    try{TimeUnit.MILLISECONDS.sleep(100);}
                                    catch(InterruptedException e){}
                                }     
                                pozycjaX=this.getPozycjaX();
                                pozycjaY=this.getPozycjaY();
                            }
                            if(this.drogaWolna()) pozycjaY--;
                        }
                        if(pozycjaY-celPodrozyY < 0){ // cel jest na dole
                            if(kierunekJazdy.equals("gora")){
                                while(!this.zawroc()){
                                    try{TimeUnit.MILLISECONDS.sleep(100);}
                                    catch(InterruptedException e){}
                                }
                            }
                            if(kierunekJazdy.equals("prawo")) {
                                while(!this.skrecWPrawo()){     // kierunek= dol
                                    try{TimeUnit.MILLISECONDS.sleep(100);}
                                    catch(InterruptedException e){}
                                }
                                pozycjaX=this.getPozycjaX();
                                pozycjaY=this.getPozycjaY();
                            }
                            else if(kierunekJazdy.equals("lewo")) {
                                while(!this.skrecWLewo()){      // kierunek= dol
                                    try{TimeUnit.MILLISECONDS.sleep(100);}
                                    catch(InterruptedException e){}
                                }
                                pozycjaX=this.getPozycjaX();
                                pozycjaY=this.getPozycjaY();
                            }
                            if(this.drogaWolna()) pozycjaY++;
                        }
                    }
                }
                else if(Math.abs(pozycjaY-celPodrozyY) <= 40){ // JEST NA GRANICY SKRZYZOWANIA NA KTORYM MUSI SKRECIC lub ZACZAL NA WLASCIWEJ ULICY =jazda w poziomie
                    if(pozycjaX != celPodrozyX){
                        if(celPodrozyX-pozycjaX > 0){   // cel jest z prawej
                            if(kierunekJazdy.equals("lewo")) {
                                while(!this.zawroc()){
                                    try{TimeUnit.MILLISECONDS.sleep(100);}
                                    catch(InterruptedException e){}
                                }
                            }
                            if(kierunekJazdy.equals("gora")) {
                                while(!this.skrecWPrawo()){     // kierunek= prawo
                                    try{TimeUnit.MILLISECONDS.sleep(100);}
                                    catch(InterruptedException e){}
                                }
                                pozycjaX=this.getPozycjaX();
                                pozycjaY=this.getPozycjaY();
                            }
                            else if(kierunekJazdy.equals("dol")) {
                                while(!this.skrecWLewo()){      // kierunek= prawo
                                    try{TimeUnit.MILLISECONDS.sleep(100);}
                                    catch(InterruptedException e){}
                                }
                                pozycjaX=this.getPozycjaX();
                                pozycjaY=this.getPozycjaY();
                            }
                            if(this.drogaWolna()) pozycjaX++;
                        }
                        else if(celPodrozyX-pozycjaX < 0){ // cel jest z lewej
                            if(kierunekJazdy.equals("prawo")) {
                                while(!this.zawroc()){
                                    try{TimeUnit.MILLISECONDS.sleep(100);}
                                    catch(InterruptedException e){}
                                }
                            }
                            if(kierunekJazdy.equals("gora")) {
                                while(!this.skrecWLewo()){      // kierunek= lewo
                                    try{TimeUnit.MILLISECONDS.sleep(100);}
                                    catch(InterruptedException e){}
                                }
                                pozycjaX=this.getPozycjaX();
                                pozycjaY=this.getPozycjaY();
                            }
                            else if(kierunekJazdy.equals("dol")) {
                                while(!this.skrecWPrawo()){     // kierunek= lewo
                                    try{TimeUnit.MILLISECONDS.sleep(100);}
                                    catch(InterruptedException e){}
                                }
                                pozycjaX=this.getPozycjaX();
                                pozycjaY=this.getPozycjaY();
                            }
                            if(this.drogaWolna()) pozycjaX--;
                        }
                    }
                    else if(pozycjaX == celPodrozyX){   // JEST NA WLASCIWEJ POZYCJI
                        if(bagaznik.isEmpty() || naglyPowrot) wTrasie=false;
                        if(naglyPowrot){
                            lockKolejkaZamowien.lock();
                            try{
                                while(!bagaznik.isEmpty()){
                                    kolejkaZamowien.add(bagaznik.poll());
                                }
                            }finally{lockKolejkaZamowien.unlock();}
                        }
                        else if(nrDomu%2 == 0 && kierunekJazdy.equals("prawo")) this.zawroc();
                        else if(nrDomu%2 == 1 && kierunekJazdy.equals("lewo")) this.zawroc();
                        try{
                            if(!naglyPowrot && !bagaznik.isEmpty()) TimeUnit.MILLISECONDS.sleep(2000);
                        }
                        catch(InterruptedException e){}
                        if(!bagaznik.isEmpty()) klienci.get(bagaznik.poll().getAdres()).zmniejszIloscZamowien();
                    }
                }
                if(this.getPozycjaX() != pozycjaX || this.getPozycjaY() != pozycjaY) garaz.get(nrRej).setStanBaku(garaz.get(nrRej).getStanBaku()-1);
                this.aktualizujPozycje(pozycjaX,pozycjaY);
                czekaj=30;
                if(!wTrasie){
                    restauracja.getGenerator().zwolnijPixele(this.getPozycjaX(), this.getPozycjaY()-40, this.getPozycjaX()+60, this.getPozycjaY());
                }
                //</editor-fold>
            }
            try{
                TimeUnit.MILLISECONDS.sleep(czekaj);
            }
            catch(InterruptedException e){}
        }
    }
    
    public int policzOdleglosc(int x1, int y1, int x2, int y2){
        return Math.abs(x1-x2)+Math.abs(y1-y2);
    }
    
    public String outDane(){
        String dane = "";
        dane=dane.concat("nazwa: "+nazwa+'\n');
        dane=dane.concat("pesel: "+pesel+'\n');
        dane=dane.concat("Pojazd:\n"+garaz.get(nrRej).outDane()+'\n');
        dane=dane.concat("Zamowien: "+bagaznik.size()+'\n');
        return dane;  
    }
    
    public void setNaglyPowrot(boolean naglyPowrot) {
        this.naglyPowrot = naglyPowrot;
    }
    
    public String getKierunekJazdy(){
        return kierunekJazdy;
    }

    public boolean iswTrasie() {
        return wTrasie;
    }

    public String getPesel() {
        return pesel;
    }
    
    public String getNrRej(){
        return nrRej;
    }
    
    public int najblizszeSkrzyzowanie(int pozycjaX){
        int blizej;
        if(Math.abs(260-pozycjaX) < Math.abs(908-pozycjaX)) blizej=260;
        else blizej=908;
        return blizej;
    }
    
    public boolean zawroc(){
        if(kierunekJazdy.equals("prawo")){  // ZAWRACA Z DOLNEGO PASA NA GORNY
            lockPixele.lock();
            try{
                if(restauracja.getGenerator().sprawdzPixele(this.getPozycjaX(), this.getPozycjaY()-40, this.getPozycjaX()+60, this.getPozycjaY()+40)){
                    restauracja.getGenerator().zajmijPixele(this.getPozycjaX(), this.getPozycjaY()-40, this.getPozycjaX()+60, this.getPozycjaY()+40, pesel);
                    restauracja.getGenerator().zwolnijPixele(this.getPozycjaX()-60, this.getPozycjaY(), this.getPozycjaX(), this.getPozycjaY()+40);
                }
                else return false;
            }finally {lockPixele.unlock();}
            for (int i = 0; i < 60; i++) {
                this.setPozycja(this.getPozycjaX()+1, this.getPozycjaY());
                garaz.get(nrRej).setStanBaku(garaz.get(nrRej).getStanBaku()-1);
                try{
                TimeUnit.MILLISECONDS.sleep(30);
                }catch(InterruptedException e){}
            }
            kierunekJazdy="gora";
            ikona=ikonaGora;
            this.setPozycja(this.getPozycjaX()-50, this.getPozycjaY()-20);
            for (int i = 0; i < 20; i++) {
                this.setPozycja(this.getPozycjaX(), this.getPozycjaY()-1);
                garaz.get(nrRej).setStanBaku(garaz.get(nrRej).getStanBaku()-1);
                try{
                TimeUnit.MILLISECONDS.sleep(30);
                }catch(InterruptedException e){}
            }
            kierunekJazdy="lewo";
            ikona=ikonaLewo;
            this.setPozycja(this.getPozycjaX()-10, this.getPozycjaY()+40);
            lockPixele.lock();
            try{
                restauracja.getGenerator().zwolnijPixele(this.getPozycjaX(), this.getPozycjaY()-1, this.getPozycjaX()+60, this.getPozycjaY()+40);
            }finally{ lockPixele.unlock();}

        }
        else if(kierunekJazdy.equals("lewo")){ // ZAWRACA Z GORNEGO PASA NA DOLNY
            lockPixele.lock();
            try{
                if(restauracja.getGenerator().sprawdzPixele(this.getPozycjaX()-60, this.getPozycjaY()-40, this.getPozycjaX(), this.getPozycjaY()+40)){
                    restauracja.getGenerator().zajmijPixele(this.getPozycjaX()-60, this.getPozycjaY()-40, this.getPozycjaX(), this.getPozycjaY()+40, pesel);
                    restauracja.getGenerator().zwolnijPixele(this.getPozycjaX(), this.getPozycjaY()-40, this.getPozycjaX()+60, this.getPozycjaY());
                }
                else return false;
            }finally {lockPixele.unlock();}
            for (int i = 0; i < 60; i++) {
                this.setPozycja(this.getPozycjaX()-1, this.getPozycjaY());
                garaz.get(nrRej).setStanBaku(garaz.get(nrRej).getStanBaku()-1);
                try{
                TimeUnit.MILLISECONDS.sleep(30);
                }catch(InterruptedException e){}
            }
            kierunekJazdy="dol";
            ikona=ikonaDol;
            this.setPozycja(this.getPozycjaX()+50, this.getPozycjaY()+20);
            for (int i = 0; i < 20; i++) {
                this.setPozycja(this.getPozycjaX(), this.getPozycjaY()+1);
                garaz.get(nrRej).setStanBaku(garaz.get(nrRej).getStanBaku()-1);
                try{
                TimeUnit.MILLISECONDS.sleep(30);
                }catch(InterruptedException e){}
            }
            kierunekJazdy="prawo";
            ikona=ikonaPrawo;
            this.setPozycja(this.getPozycjaX()+10, this.getPozycjaY()-40);
            lockPixele.lock();
            try{
            restauracja.getGenerator().zwolnijPixele(this.getPozycjaX()-60, this.getPozycjaY()-40, this.getPozycjaX(), this.getPozycjaY()+1);
            }finally{ lockPixele.unlock();}
        }
        else if(kierunekJazdy.equals("gora")){ // ZAWRACA Z PRAWEGO PASA NA LEWY (w pionie)
            lockPixele.lock();
            try{
                if(restauracja.getGenerator().sprawdzPixele(this.getPozycjaX()-60, this.getPozycjaY()-60, this.getPozycjaX()+60, this.getPozycjaY())){
                    restauracja.getGenerator().zajmijPixele(this.getPozycjaX()-60, this.getPozycjaY()-60, this.getPozycjaX()+60, this.getPozycjaY(), pesel);
                    restauracja.getGenerator().zwolnijPixele(this.getPozycjaX(), this.getPozycjaY(), this.getPozycjaX()+60, this.getPozycjaY()+60);
                }
            }finally{lockPixele.unlock();}
            for (int i = 0; i < 60; i++) {
                this.setPozycja(this.getPozycjaX(), this.getPozycjaY()-1);
                garaz.get(nrRej).setStanBaku(garaz.get(nrRej).getStanBaku()-1);
                try{
                TimeUnit.MILLISECONDS.sleep(30);
                }catch(InterruptedException e){}
            }
            kierunekJazdy="lewo";
            ikona=ikonaLewo;
            this.setPozycja(this.getPozycjaX()-10, this.getPozycjaY()+40);
            for (int i = 0; i < 50; i++) {
                this.setPozycja(this.getPozycjaX()-1, this.getPozycjaY());
                garaz.get(nrRej).setStanBaku(garaz.get(nrRej).getStanBaku()-1);
                try{
                TimeUnit.MILLISECONDS.sleep(30);
                }catch(InterruptedException e){}
            }
            kierunekJazdy="dol";
            ikona=ikonaDol;
            this.setPozycja(this.getPozycjaX()+60, this.getPozycjaY()+20);
            lockPixele.lock();
            try{
            restauracja.getGenerator().zwolnijPixele(this.getPozycjaX()-61, this.getPozycjaY()-61, this.getPozycjaX()+60, this.getPozycjaY());
            }finally{ lockPixele.unlock();}
        }
        return true;
    }
    
    public boolean drogaWolna(){
        boolean odpowiedz=false;
        lockPixele.lock();
        try{
            switch(kierunekJazdy){
                case "prawo": odpowiedz=restauracja.getGenerator().sprawdzPixele(this.getPozycjaX(), this.getPozycjaY(), this.getPozycjaX()+2, this.getPozycjaY()+40);
                    break;
                case "lewo": odpowiedz=restauracja.getGenerator().sprawdzPixele(this.getPozycjaX()-2, this.getPozycjaY()-40, this.getPozycjaX(), this.getPozycjaY());
                    break;
                case "gora": odpowiedz=restauracja.getGenerator().sprawdzPixele(this.getPozycjaX(), this.getPozycjaY()-2, this.getPozycjaX()+60, this.getPozycjaY());
                    break;
                case "dol": odpowiedz=restauracja.getGenerator().sprawdzPixele(this.getPozycjaX()-60, this.getPozycjaY(), this.getPozycjaX(), this.getPozycjaY()+2);
                    break;
            }
        }finally{ lockPixele.unlock();}
        return odpowiedz;
    }
    
    public boolean skrecWLewo(){
        int pozX, pozY, pomX, pomY;
        pozX=this.getPozycjaX(); pomX=pozX;
        pozY=this.getPozycjaY(); pomY=pozY;
        switch(kierunekJazdy){
            case "prawo":
                lockPixele.lock();
                try{
                    if(restauracja.getGenerator().sprawdzPixele(pozX, pozY, pozX+120, pozY+40) && restauracja.getGenerator().sprawdzPixele(pozX+60, pozY-80, pozX+120, pozY)){
                        restauracja.getGenerator().zajmijPixele(pozX, pozY, pozX+120, pozY+40, pesel);
                        restauracja.getGenerator().zajmijPixele(pozX+60, pozY-80, pozX+120, pozY, pesel);
                        restauracja.getGenerator().zwolnijPixele(pozX-60, pozY, pozX, pozY+40);
                    }
                    else return false;
                }finally {lockPixele.unlock();}
                for (int i = 0; i < 110; i++) {
                    this.setPozycja(++pozX, pozY);
                    garaz.get(nrRej).setStanBaku(garaz.get(nrRej).getStanBaku()-1);
                    try{
                        TimeUnit.MILLISECONDS.sleep(30);
                    }catch(InterruptedException e){}
                }
                kierunekJazdy="gora";
                ikona=ikonaGora;
                pozX=pozX-50;
                pozY=pozY-20;
                this.setPozycja(pozX, pozY);
                for (int i = 0; i < 60; i++) {
                    this.setPozycja(pozX, --pozY);
                    garaz.get(nrRej).setStanBaku(garaz.get(nrRej).getStanBaku()-1);
                    try{
                        TimeUnit.MILLISECONDS.sleep(30);
                    }catch(InterruptedException e){}
                }
                lockPixele.lock();
                try{
                    restauracja.getGenerator().zwolnijPixele(pomX, pomY, pomX+120, pomY+40);
                    restauracja.getGenerator().zwolnijPixele(pozX, pozY+59, pozX+60, pozY+80);
                }finally{ lockPixele.unlock();}
                break;
            case "lewo":    
                lockPixele.lock();
                try{
                    if(restauracja.getGenerator().sprawdzPixele(pozX-120, pozY-40, pozX, pozY) && restauracja.getGenerator().sprawdzPixele(pozX-120, pozY, pozX-60, pozY+80)){
                        restauracja.getGenerator().zajmijPixele(pozX-120, pozY-40, pozX, pozY, pesel);
                        restauracja.getGenerator().zajmijPixele(pozX-120, pozY, pozX-60, pozY+80, pesel);
                        restauracja.getGenerator().zwolnijPixele(pozX, pozY-40, pozX+60, pozY);
                    }
                    else return false;
                }finally {lockPixele.unlock();}
                for (int i = 0; i < 110; i++) {
                    this.setPozycja(--pozX, pozY);
                    garaz.get(nrRej).setStanBaku(garaz.get(nrRej).getStanBaku()-1);
                    try{
                        TimeUnit.MILLISECONDS.sleep(30);
                    }catch(InterruptedException e){}
                }
                kierunekJazdy="dol";
                ikona=ikonaDol;
                pozX=pozX+50;
                pozY=pozY+20;
                this.setPozycja(pozX, pozY);
                for (int i = 0; i < 60; i++) {
                    this.setPozycja(pozX, ++pozY);
                    garaz.get(nrRej).setStanBaku(garaz.get(nrRej).getStanBaku()-1);
                    try{
                        TimeUnit.MILLISECONDS.sleep(30);
                    }catch(InterruptedException e){}
                }
                lockPixele.lock();
                try{
                    restauracja.getGenerator().zwolnijPixele(pomX-120, pomY-40, pomX, pomY);
                    restauracja.getGenerator().zwolnijPixele(pozX-60, pozY-80, pozX, pozY-59);
                }finally{ lockPixele.unlock();}
                break;
            case "gora":    
                lockPixele.lock();
                try{
                    if(restauracja.getGenerator().sprawdzPixele(pozX, pozY-80, pozX+60, pozY) && restauracja.getGenerator().sprawdzPixele(pozX-80, pozY-80, pozX, pozY-40)){
                        restauracja.getGenerator().zajmijPixele(pozX, pozY-80, pozX+60, pozY, pesel);
                        restauracja.getGenerator().zajmijPixele(pozX-80, pozY-80, pozX, pozY-40, pesel);
                        restauracja.getGenerator().zwolnijPixele(pozX, pozY, pozX+60, pozY+60);
                    }
                    else return false;
                }finally {lockPixele.unlock();}
                for (int i = 0; i < 80; i++) {
                    this.setPozycja(pozX, --pozY);
                    garaz.get(nrRej).setStanBaku(garaz.get(nrRej).getStanBaku()-1);
                    try{
                        TimeUnit.MILLISECONDS.sleep(30);
                    }catch(InterruptedException e){}
                }
                kierunekJazdy="lewo";
                ikona=ikonaLewo;
                pozX=pozX-10;
                pozY=pozY+40;
                this.setPozycja(pozX, pozY);
                for (int i = 0; i < 70; i++) {
                    this.setPozycja(--pozX, pozY);
                    garaz.get(nrRej).setStanBaku(garaz.get(nrRej).getStanBaku()-1);
                    try{
                        TimeUnit.MILLISECONDS.sleep(30);
                    }catch(InterruptedException e){}
                }
                lockPixele.lock();
                try{
                    restauracja.getGenerator().zwolnijPixele(pomX, pomY-80, pomX+60, pomY);
                    restauracja.getGenerator().zwolnijPixele(pozX+59, pozY-40, pozX+80, pozY);
                }finally{ lockPixele.unlock();}
                break;
            case "dol":     
                lockPixele.lock();
                try{
                    if(restauracja.getGenerator().sprawdzPixele(pozX-60, pozY, pozX, pozY+80) && restauracja.getGenerator().sprawdzPixele(pozX, pozY+40, pozX+80, pozY+80)){
                        restauracja.getGenerator().zajmijPixele(pozX-60, pozY, pozX, pozY+80, pesel);
                        restauracja.getGenerator().zajmijPixele(pozX, pozY+40, pozX+80, pozY+80, pesel);
                        restauracja.getGenerator().zwolnijPixele(pozX-60, pozY-60, pozX, pozY);
                    }
                    else return false;
                }finally {lockPixele.unlock();}
                for (int i = 0; i < 80; i++) {
                    this.setPozycja(pozX, ++pozY);
                    garaz.get(nrRej).setStanBaku(garaz.get(nrRej).getStanBaku()-1);
                    try{
                        TimeUnit.MILLISECONDS.sleep(30);
                    }catch(InterruptedException e){}
                }
                kierunekJazdy="prawo";
                ikona=ikonaPrawo;
                pozX=pozX+10;
                pozY=pozY-40;
                this.setPozycja(pozX, pozY);
                for (int i = 0; i < 70; i++) {
                    this.setPozycja(++pozX, pozY);
                    garaz.get(nrRej).setStanBaku(garaz.get(nrRej).getStanBaku()-1);
                    try{
                        TimeUnit.MILLISECONDS.sleep(30);
                    }catch(InterruptedException e){}
                }
                lockPixele.lock();
                try{
                    restauracja.getGenerator().zwolnijPixele(pomX-60, pomY, pomX, pomY+80);
                    restauracja.getGenerator().zwolnijPixele(pozX-80, pozY, pozX-59, pozY+40);
                }finally{ lockPixele.unlock();}
                break;
        }
        return true;
    }
    
    public boolean skrecWPrawo(){
        int pozX, pozY;
        pozX=this.getPozycjaX();
        pozY=this.getPozycjaY();
        switch(kierunekJazdy){
            case "prawo":
                lockPixele.lock();
                try{
                    if(restauracja.getGenerator().sprawdzPixele(pozX, pozY, pozX+60, pozY+80)){
                        restauracja.getGenerator().zajmijPixele(pozX, pozY, pozX+60, pozY+80, pesel);
                        restauracja.getGenerator().zwolnijPixele(pozX-60, pozY, pozX, pozY+40);
                    }
                    else return false;
                }finally {lockPixele.unlock();}
                for (int i = 0; i < 50; i++) {
                    this.setPozycja(++pozX, pozY);
                    garaz.get(nrRej).setStanBaku(garaz.get(nrRej).getStanBaku()-1);
                    try{
                        TimeUnit.MILLISECONDS.sleep(30);
                    }catch(InterruptedException e){}
                }
                kierunekJazdy="dol";
                ikona=ikonaDol;
                pozX=pozX+10;
                pozY=pozY+60;
                this.setPozycja(pozX, pozY);
                for (int i = 0; i < 20; i++) {
                    this.setPozycja(pozX, ++pozY);
                    garaz.get(nrRej).setStanBaku(garaz.get(nrRej).getStanBaku()-1);
                    try{
                        TimeUnit.MILLISECONDS.sleep(30);
                    }catch(InterruptedException e){}
                }
                lockPixele.lock();
                try{
                    restauracja.getGenerator().zwolnijPixele(pozX-60, pozY-80, pozX, pozY-59);
                }finally{ lockPixele.unlock();}
                break;
            case "lewo":    
                lockPixele.lock();
                try{
                    if(restauracja.getGenerator().sprawdzPixele(pozX-60, pozY-80, pozX, pozY)){
                        restauracja.getGenerator().zajmijPixele(pozX-60, pozY-80, pozX, pozY, pesel);
                        restauracja.getGenerator().zwolnijPixele(pozX, pozY-40, pozX+60, pozY);
                    }
                    else return false;
                }finally {lockPixele.unlock();}
                for (int i = 0; i < 50; i++) {
                    this.setPozycja(--pozX, pozY);
                    garaz.get(nrRej).setStanBaku(garaz.get(nrRej).getStanBaku()-1);
                    try{
                        TimeUnit.MILLISECONDS.sleep(30);
                    }catch(InterruptedException e){}
                }
                kierunekJazdy="gora";
                ikona=ikonaGora;
                pozX=pozX-10;
                pozY=pozY-60;
                this.setPozycja(pozX, pozY);
                for (int i = 0; i < 20; i++) {
                    this.setPozycja(pozX, --pozY);
                    garaz.get(nrRej).setStanBaku(garaz.get(nrRej).getStanBaku()-1);
                    try{
                        TimeUnit.MILLISECONDS.sleep(30);
                    }catch(InterruptedException e){}
                }
                lockPixele.lock();
                try{
                    restauracja.getGenerator().zwolnijPixele(pozX, pozY+59, pozX+60, pozY+80);
                }finally{ lockPixele.unlock();}
                break;
            case "gora":    
                lockPixele.lock();
                try{
                    if(restauracja.getGenerator().sprawdzPixele(pozX, pozY-40, pozX+80, pozY)){
                        restauracja.getGenerator().zajmijPixele(pozX, pozY-40, pozX+80, pozY, pesel);
                        restauracja.getGenerator().zwolnijPixele(pozX, pozY, pozX+60, pozY+60);
                    }
                    else return false;
                }finally {lockPixele.unlock();}
                for (int i = 0; i < 40; i++) {
                    this.setPozycja(pozX, --pozY);
                    garaz.get(nrRej).setStanBaku(garaz.get(nrRej).getStanBaku()-1);
                    try{
                        TimeUnit.MILLISECONDS.sleep(30);
                    }catch(InterruptedException e){}
                }
                kierunekJazdy="prawo";
                ikona=ikonaPrawo;
                pozX=pozX+70;
                this.setPozycja(pozX, pozY);
                for (int i = 0; i < 10; i++) {
                    this.setPozycja(++pozX, pozY);
                    garaz.get(nrRej).setStanBaku(garaz.get(nrRej).getStanBaku()-1);
                    try{
                        TimeUnit.MILLISECONDS.sleep(30);
                    }catch(InterruptedException e){}
                }
                lockPixele.lock();
                try{
                    restauracja.getGenerator().zwolnijPixele(pozX-80, pozY, pozX-59, pozY+40);
                }finally{ lockPixele.unlock();}
                break;
            case "dol":     
                lockPixele.lock();
                try{
                    if(restauracja.getGenerator().sprawdzPixele(pozX-80, pozY, pozX, pozY+40)){
                        restauracja.getGenerator().zajmijPixele(pozX-80, pozY, pozX, pozY+40, pesel);
                        restauracja.getGenerator().zwolnijPixele(pozX-60, pozY-60, pozX, pozY);
                    }
                    else return false;
                }finally {lockPixele.unlock();}
                for (int i = 0; i < 40; i++) {
                    this.setPozycja(pozX, ++pozY);
                    garaz.get(nrRej).setStanBaku(garaz.get(nrRej).getStanBaku()-1);
                    try{
                        TimeUnit.MILLISECONDS.sleep(30);
                    }catch(InterruptedException e){}
                }
                kierunekJazdy="lewo";
                ikona=ikonaLewo;
                pozX=pozX-70;
                this.setPozycja(pozX, pozY);
                for (int i = 0; i < 10; i++) {
                    this.setPozycja(--pozX, pozY);
                    garaz.get(nrRej).setStanBaku(garaz.get(nrRej).getStanBaku()-1);
                    try{
                        TimeUnit.MILLISECONDS.sleep(30);
                    }catch(InterruptedException e){}
                }
                lockPixele.lock();
                try{
                    restauracja.getGenerator().zwolnijPixele(pozX+59, pozY-40, pozX+80, pozY);
                }finally{ lockPixele.unlock();}
                break;
        }
        return true;
    }
    
    public void aktualizujPozycje(int pozycjaX, int pozycjaY){
        lockPixele.lock();
        try{
            
            switch(kierunekJazdy){
                case "prawo":   restauracja.getGenerator().zwolnijPixele(this.getPozycjaX()-60, this.getPozycjaY(), this.getPozycjaX(), this.getPozycjaY()+40);
                                this.setPozycja(pozycjaX, pozycjaY);
                                restauracja.getGenerator().zajmijPixele(this.getPozycjaX()-60, this.getPozycjaY(), this.getPozycjaX(), this.getPozycjaY()+40, pesel);
                    break;
                case "lewo":    restauracja.getGenerator().zwolnijPixele(this.getPozycjaX(), this.getPozycjaY()-40, this.getPozycjaX()+60, this.getPozycjaY());
                                this.setPozycja(pozycjaX, pozycjaY);
                                restauracja.getGenerator().zajmijPixele(this.getPozycjaX(), this.getPozycjaY()-40, this.getPozycjaX()+60, this.getPozycjaY(), pesel);
                    break;
                case "gora":    restauracja.getGenerator().zwolnijPixele(this.getPozycjaX(), this.getPozycjaY(), this.getPozycjaX()+60, this.getPozycjaY()+60);
                                this.setPozycja(pozycjaX, pozycjaY);
                                restauracja.getGenerator().zajmijPixele(this.getPozycjaX(), this.getPozycjaY(), this.getPozycjaX()+60, this.getPozycjaY()+60, pesel);
                    break;
                case "dol":     restauracja.getGenerator().zwolnijPixele(this.getPozycjaX()-60, this.getPozycjaY()-60, this.getPozycjaX(), this.getPozycjaY());
                                this.setPozycja(pozycjaX, pozycjaY);
                                restauracja.getGenerator().zajmijPixele(this.getPozycjaX()-60, this.getPozycjaY()-60, this.getPozycjaX(), this.getPozycjaY(), pesel);
                    break;
            }
        }finally{ lockPixele.unlock();}
    }
    
    public void zwolnijSwojePixele(){
        lockPixele.lock();
        try{
            
            switch(kierunekJazdy){
                case "prawo":   restauracja.getGenerator().zwolnijPixele(this.getPozycjaX()-60, this.getPozycjaY(), this.getPozycjaX(), this.getPozycjaY()+40);
                    break;
                case "lewo":    restauracja.getGenerator().zwolnijPixele(this.getPozycjaX(), this.getPozycjaY()-40, this.getPozycjaX()+60, this.getPozycjaY());
                    break;
                case "gora":    restauracja.getGenerator().zwolnijPixele(this.getPozycjaX(), this.getPozycjaY(), this.getPozycjaX()+60, this.getPozycjaY()+60);
                    break;
                case "dol":     restauracja.getGenerator().zwolnijPixele(this.getPozycjaX()-60, this.getPozycjaY()-60, this.getPozycjaX(), this.getPozycjaY());
                    break;
            }
        }finally{ lockPixele.unlock();}
    }
    
    public boolean getRun(){
        return run;
    }

    public String getTyp() {
        return typ;
    }

    public void setTyp(String typ) {
        this.typ = typ;
    }
    
    public void ustawWielblada(){
        ikona=DolaczObrazek("/img/rumak_prawo.png");
        ikonaPrawo=DolaczObrazek("/img/rumak_prawo.png");
        ikonaGora=DolaczObrazek("/img/rumak_gora.png");
        ikonaLewo=DolaczObrazek("/img/rumak_lewo.png");
        ikonaDol=DolaczObrazek("/img/rumak_dol.png");
    }
    
    public void ustawSlonia(){
        ikona=DolaczObrazek("/img/slon_prawo.png");
        ikonaPrawo=DolaczObrazek("/img/slon_prawo.png");
        ikonaGora=DolaczObrazek("/img/slon_gora.png");
        ikonaLewo=DolaczObrazek("/img/slon_lewo.png");
        ikonaDol=DolaczObrazek("/img/slon_dol.png");
    }
}
