/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nazwaprojektu;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import static nazwaprojektu.NazwaProjektu.lockKlienci;
import static nazwaprojektu.NazwaProjektu.lockEkipa;
import static nazwaprojektu.NazwaProjektu.lockGaraz;
import static nazwaprojektu.NazwaProjektu.lockKolejkaZamowien;
import static nazwaprojektu.NazwaProjektu.lockMenu;
import static nazwaprojektu.NazwaProjektu.lockMenuZestawow;
import static nazwaprojektu.NazwaProjektu.lockPixele;

/**
 *
 * @author Kubik
 */
public class Restauracja{
    // GLOBALNE ZMIENNE
    public static  List<Posilek> menu = new ArrayList<>();
    public static List<Zestaw> menuZestawow = new ArrayList<>();
    public static HashMap<String,Pojazd> garaz = new HashMap<>();
    public static HashMap<String,Dostawca> ekipa = new HashMap<>();
    public static List<Zamowienie> kolejkaZamowien = new LinkedList<>();
    public static HashMap<String,Klient> klienci = new HashMap<>();
    private GeneratorAdresow generator;
    private int pozycjaX;
    private int pozycjaY;
    public static BufferedImage mapa;
    
    public Restauracja(){
        generator = new GeneratorAdresow();
        pozycjaX=36;
        pozycjaY=625;
        int nr;
        try {
            nr = generator.przydzielNrPosilku();
            Posilek p = new Posilek(nr);
            Zestaw z = new Zestaw(p.getRozmiar(),p.getNazwa(),p.getCena(),p.getCzasPrzygotowania());
            menu.add(p);
            menuZestawow.add(z);
        } catch (Exception e) {
            System.err.println("Blad dodania posilku");
        }
        mapa = null;
        try {
                mapa =ImageIO.read(getClass().getResource("/img/mapa_terenu.png"));
        } catch (IOException e) {
                System.err.println("Blad odczytu mapy");
        }
    }
    
    public boolean wydajGotoweZamowienia(){
        return true;
    }

    public String dodajPosilek(){
        int nr;
        try {
            nr = generator.przydzielNrPosilku();
            Posilek p = new Posilek(nr);
            Zestaw z = new Zestaw(p.getRozmiar(),p.getNazwa(),p.getCena(),p.getCzasPrzygotowania());
            menu.add(p);
            menuZestawow.add(z);
        } catch (Exception e) {
            //e.printStackTrace();
            return "menu jest pełne";
        }
        return "Posiłek dodano pomyślnie!";
    }
    
    public String dodajDostawce(){
        lockEkipa.lock();
        try{
            if(ekipa.size()<100) {
                Dostawca d= new Dostawca(generator);
                ekipa.put(d.getPesel(), d);
                new Thread(d).start();
                return d.getPesel();
            }
            else{
                return "Restauracja może zatrudnić max 100 dostawców";
            }
        }finally{
            lockEkipa.unlock();
        }
    }
    
    public String dodajKlientaOka(){
        String adr;
        try{
            adr=generator.przydzielAdres();                 // rzuca wyjatkiem
            Klient k = new KlientOkazjonalny(adr,generator);
            lockKlienci.lock();
            try{
                klienci.put(adr,k);
                new Thread(k).start();
            }finally{
                lockKlienci.unlock();
            }
        }
        catch(Exception e){
            return "brak miejsc";
            //e.printStackTrace();
        }
        return adr;
    }
    
    public String dodajKlientaSta(){
        String adr;
        try{
            adr=generator.przydzielAdres();                 // rzuca wyjatkiem
            Klient k = new KlientStaly(adr,generator);
            klienci.put(adr,k);
            new Thread(k).start();
        }
        catch(Exception e){
            return "brak miejsc";
            //e.printStackTrace();
        }
        return adr;
    }
    
    public String dodajKlientaFir(){
        String adr;
        try{
            adr=generator.przydzielAdres();                 // rzuca wyjatkiem
            Klient k = new KlientFirmowy(adr,generator);
            klienci.put(adr,k);
            new Thread(k).start();
        }
        catch(Exception e){
            return "brak miejsc";
            //e.printStackTrace();
        }
        return adr;
    }
    
    public String usunKlienta(String adres){
        lockKlienci.lock();
        try{
            generator.setTablicaAdr(adres);
            if(Integer.parseInt(adres.substring(adres.indexOf(" ")+1))%2 == 0){
                generator.zwolnijPixele(klienci.get(adres).getPozycjaX()-30, klienci.get(adres).getPozycjaY()-100, klienci.get(adres).getPozycjaX()+30, klienci.get(adres).getPozycjaY()-40);
            }
            else {
                generator.zwolnijPixele(klienci.get(adres).getPozycjaX()-30, klienci.get(adres).getPozycjaY()+40, klienci.get(adres).getPozycjaX()+30, klienci.get(adres).getPozycjaY()+100);
            }
            klienci.get(adres).setRun(false);
            klienci.remove(adres);
            return "Usunięto Klienta";
        } finally{
            lockKlienci.unlock();
        }
    }
    
    public String usunDostawce(String pesel){
        lockEkipa.lock();
        try{
            garaz.remove(ekipa.get(pesel).getNrRej());
            ekipa.get(pesel).setRun(false);
            while(ekipa.get(pesel).getRun()){
                try{TimeUnit.MILLISECONDS.sleep(5);}
            catch(InterruptedException e){}
            }
            ekipa.get(pesel).zwolnijSwojePixele();
            ekipa.remove(pesel);
            return "Usunięto Dostawcę";
        } finally{
            lockEkipa.unlock();
        }
    }

    public GeneratorAdresow getGenerator() {
        return generator;
    }
    
    public void zapiszDane() throws IOException{
        ObjectOutputStream out = new ObjectOutputStream(
                               new BufferedOutputStream(
                                 new FileOutputStream("./zapis.ser")));
        int size;
        Set<String> keys;
        out.writeObject(generator);
        lockMenu.lock();
        try{
            size=menu.size();
            out.writeObject(Integer.toString(size));
            for (int i = 0; i < size; i++) {
                out.writeObject(menu.get(i));
            }
        }finally{lockMenu.unlock();}
        
        lockMenuZestawow.lock();
        try{
            size=menuZestawow.size();
            out.writeObject(Integer.toString(size));
            for (int i = 0; i < size; i++) {
                out.writeObject(menuZestawow.get(i));
            }
        }finally{lockMenuZestawow.unlock();}
        
        lockGaraz.lock();
        try{
            size=garaz.size();
            out.writeObject(Integer.toString(size));
            keys=garaz.keySet();
            for (String key : keys) {
                out.writeObject(garaz.get(key));
            }
        }finally{lockGaraz.unlock();}
        
        lockEkipa.lock();
        try{
            size=ekipa.size();
            out.writeObject(Integer.toString(size));
            keys=ekipa.keySet();
            for (String key : keys) {
                out.writeObject(ekipa.get(key).getTyp());
                out.writeObject(ekipa.get(key));
            }
        }finally{lockEkipa.unlock();}
        
        lockKlienci.lock();
        try{
            size=klienci.size();
            out.writeObject(Integer.toString(size));
            keys=klienci.keySet();
            for (String key : keys) {
                out.writeObject(klienci.get(key).getTyp());
                out.writeObject(klienci.get(key));
            }
        }finally{lockKlienci.unlock();}
        
        lockKolejkaZamowien.lock();
        try{
            size=kolejkaZamowien.size();
            out.writeObject(Integer.toString(size));
            for (int i = 0; i < size; i++) {
                out.writeObject(kolejkaZamowien.get(i));
            }
        }finally{lockKolejkaZamowien.unlock();}
        out.close();
    }
    
    public void wczytajDane() throws IOException, ClassNotFoundException{
        ObjectInputStream in = new ObjectInputStream(
                             new BufferedInputStream(
                               new FileInputStream("./zapis.ser")));
        int size;
        String dane;
        generator = (GeneratorAdresow) in.readObject();
        generator.wyczyscPixele();
        
        dane = (String) in.readObject();
        size=Integer.parseInt(dane);
        menu.clear();
        for (int i = 0; i < size; i++) {
            menu.add((Posilek) in.readObject());
        }
        
        dane = (String) in.readObject();
        size=Integer.parseInt(dane);
        menuZestawow.clear();
        for (int i = 0; i < size; i++) {
            menuZestawow.add((Zestaw) in.readObject());
        }
        
        dane = (String) in.readObject();
        size=Integer.parseInt(dane);
        for (int i = 0; i < size; i++) {
            Pojazd p = (Pojazd) in.readObject();
            garaz.put(p.getNrRejestracyjny(), p);
        }
        
        dane = (String) in.readObject();
        size=Integer.parseInt(dane);
        for (int i = 0; i < size; i++) {
            dane = (String) in.readObject();
            Dostawca d = (Dostawca) in.readObject();
            if(dane.equals("Wielbłąd")) d.ustawWielblada();
            else if(dane.equals("Słoń")) d.ustawSlonia();
            ekipa.put(d.getPesel(), d);
            new Thread(d).start();
        }

        dane = (String) in.readObject();
        size=Integer.parseInt(dane);
        for (int i = 0; i < size; i++) {
            dane= (String) in.readObject();
            if(dane.equals("Firmowy")){
                Klient k = (KlientFirmowy) in.readObject();
                k.ustawIkone(dane);
                klienci.put(k.getAdres(), k);
                k.zaznaczNaMapiePixeli(generator);
                new Thread(k).start();
            }
            if(dane.equals("Okazjonalny")){
                Klient k = (KlientOkazjonalny) in.readObject();
                k.ustawIkone(dane);
                klienci.put(k.getAdres(), k);
                k.zaznaczNaMapiePixeli(generator);
                new Thread(k).start();
            }
            if(dane.equals("Staly")){
                Klient k = (KlientStaly) in.readObject();
                k.ustawIkone(dane);
                klienci.put(k.getAdres(), k);
                k.zaznaczNaMapiePixeli(generator);
                new Thread(k).start();
            }
            
        }
        
        dane = (String) in.readObject();
        size=Integer.parseInt(dane);
        kolejkaZamowien.clear();
        for (int i = 0; i < size; i++) {
            kolejkaZamowien.add((Zamowienie) in.readObject());
        }
        in.close();
    }
}
