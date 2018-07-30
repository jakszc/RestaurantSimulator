/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nazwaprojektu;

import java.util.*;
import java.util.concurrent.locks.*;


/**
 *
 * @author Kubik
 */

public class NazwaProjektu {
    public static Restauracja restauracja = new Restauracja();
    public static Lock lockKlienci = new ReentrantLock();
    public static Lock lockEkipa = new ReentrantLock();
    public static Lock lockGaraz = new ReentrantLock();
    public static Lock lockMenu = new ReentrantLock();
    public static Lock lockMenuZestawow = new ReentrantLock();
    public static Lock lockKolejkaZamowien = new ReentrantLock();
    public static Lock lockPixele = new ReentrantLock();
    public static String output="";
    public static int Xclick;
    public static int Yclick;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //Restauracja r = new Restauracja();
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Okno.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        //</editor-fold>

       // MouseListener Mouse = new MouseListener() {};
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Okno().setVisible(true);
            }
        });
        
        // TODO code application logic here
   
    }
}
