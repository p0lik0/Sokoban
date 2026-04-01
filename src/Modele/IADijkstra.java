package Modele;

import Global.Configuration;
import Structures.Sequence;
import Structures.FAPListe;
import java.util.concurrent.TimeUnit;

public class IADijkstra extends IA {

    int butL, butC;
    int caisseL, caisseC;
    Niveau copieSansCaisseEtPousseur;
    Niveau copie;
    int lignes, colonnes;
    private int compteurLigne = 0;

    IADijkstra(){
        butL = -1;
        butC = -1;
        caisseL = -1;
        caisseC = -1;
    }

    class Noeud implements Comparable<Noeud> {
        int caisseL, caisseC;
        int pousseurL, pousseurC;
        int distance;
        Noeud prec;

        Noeud(int caisseL, int caisseC, int pousseurL, int pousseurC, Noeud p, int butL, int butC) {
            this.caisseL = caisseL;
            this.caisseC = caisseC;
            this.prec = p;
            this.pousseurL = pousseurL;
            this.pousseurC = pousseurC;
            distance = Math.abs(butL-caisseL)+Math.abs(butC-caisseC);
        }
        @Override
        public int compareTo(Noeud n) {
            return this.distance - n.distance ;
        }
        @Override
        public String toString() {
            if(prec==null) return  "null   ->   C(" + caisseL + ", " + caisseC + ")   P(" + pousseurL + ", " + pousseurC + ")";
            else return  " C(" + prec.caisseL + "," + prec.caisseC + ")   P(" + prec.pousseurL + "," + prec.pousseurC + ")   ->   C(" + caisseL + ", " + caisseC + ")   P(" + pousseurL + ", " + pousseurC + ")";
        }
    }


    public Sequence<Coup> joue() {
        lignes = niveau.lignes();
        colonnes = niveau.colonnes();

        // chercher caisse et but
        for (int i = 0; i < lignes; i++) {
            for (int j = 0; j < colonnes; j++) {

                if (niveau.aCaisse(i, j)) {
                    caisseL = i;
                    caisseC = j;
                }

                if (niveau.aBut(i, j)) {
                    butL = i;
                    butC = j;
                }
            }
        }
        copieSansCaisseEtPousseur = niveau.clone();
        copieSansCaisseEtPousseur.videCase(niveau.lignePousseur(), niveau.colonnePousseur());
        copieSansCaisseEtPousseur.videCase(caisseL, caisseC);

        FAPListe<Noeud> file = new FAPListe<>();
        file.insere(new Noeud(caisseL, caisseC,niveau.lignePousseur(), niveau.colonnePousseur(), null, butL, butC));
        Noeud objectif = null;
        // boolean[][] visites = new boolean[lignes][colonnes];

        while (!file.estVide()) {
            Noeud n = file.extrait();
            // System.out.println("Extrait : C("+n.caisseL+","+n.caisseC+")   P("+n.pousseurL+","+n.pousseurC+")");

            if (n.caisseL == butL && n.caisseC == butC) {
                System.out.println("On a trouvé un chemin !");
                objectif = n;
                // System.out.println("objectif : "+ n.l + " " + n.c);
                Sequence<Coup> resultat = Configuration.nouvelleSequence();

                while (objectif.prec != null) {
                    Coup push = new Coup();
                    if((objectif.prec.pousseurL-objectif.pousseurL)!=0 || (objectif.prec.pousseurC-objectif.pousseurC)!=0)
                        push.deplacementPousseur( objectif.prec.pousseurL,  objectif.prec.pousseurC, objectif.pousseurL, objectif.pousseurC);
                    if((objectif.prec.caisseL-objectif.caisseL)!=0 || (objectif.prec.caisseC-objectif.caisseC)!=0)
                        push.deplacementCaisse(objectif.prec.caisseL, objectif.prec.caisseC, objectif.caisseL, objectif.caisseC);
                    System.out.println("pousseur : " + push.pousseur + "   caisse : "+ push.caisse);
                    resultat.insereTete(push);

                    objectif = objectif.prec;
                }
                return resultat;
            }
            // System.out.println("On traite : ("+n.caisseL+","+n.caisseC+")");

            // System.out.print("Ajoutés : ");
            ajouteDeplacementCaisse(file, n, n.caisseL + 1, n.caisseC);
            System.out.print("bas : ");
            System.out.println(file);
            ajouteDeplacementCaisse(file, n, n.caisseL - 1, n.caisseC);
            System.out.print("haut : ");
            System.out.println(file);
            ajouteDeplacementCaisse(file, n, n.caisseL, n.caisseC+1);
            System.out.print("droite : ");
            System.out.println(file);
            ajouteDeplacementCaisse(file, n, n.caisseL, n.caisseC-1);
            System.out.print("gauche : ");
            System.out.println(file);

        // try {
        //     TimeUnit.SECONDS.sleep(1); // Sleep for 1 second
        // } catch (InterruptedException e) {
        //     System.out.println("Thread was interrupted: " + e.getMessage());
        // }        
        }
        return Configuration.nouvelleSequence();
    }

    void ajouteDeplacementCaisse(FAPListe<Noeud> f, Noeud parent, int l, int c) {
        if (l < 0 || c < 0 || l >= lignes || c >= colonnes)
            return;
        if (!copieSansCaisseEtPousseur.estOccupable(l, c) && !copieSansCaisseEtPousseur.aBut(l, c)) {
            return;
        }
        if(l==parent.pousseurL+1 &&  c==parent.pousseurC || 
            l==parent.pousseurL-1 &&  c==parent.pousseurC ||
            l==parent.pousseurL && c==parent.pousseurC+1 ||
            l==parent.pousseurL && c==parent.pousseurC-1){
            f.insere(new Noeud(l, c, parent.caisseL, parent.caisseC, parent, butL, butC));
        }
        else {
            Noeud finCheminPousseur = chercheCheminPousseur(f, parent, l, c);
            if (finCheminPousseur != null) {
                f.insere(new Noeud(l, c, parent.caisseL, parent.caisseC, finCheminPousseur, butL, butC));
            }
        }
    }
    
    Noeud chercheCheminPousseur(FAPListe<Noeud> f, Noeud startConfig, int cibleCaisseL, int cibleCaisseC){
        boolean[][] visite = new boolean[lignes][colonnes];
        FAPListe<Noeud> fileCheminPousseur = new FAPListe<>();
        int dL = cibleCaisseL - startConfig.caisseL ;
        int dC = cibleCaisseC - startConfig.caisseC ;
        int ciblePousseurL = startConfig.caisseL-dL;
        int ciblePousseurC = startConfig.caisseC-dC;

        fileCheminPousseur.insere(new Noeud(startConfig.caisseL, startConfig.caisseC, startConfig.pousseurL, startConfig.pousseurC, null, ciblePousseurL, ciblePousseurC));

        Noeud objectif = null;
        if(!copieSansCaisseEtPousseur.estOccupable(ciblePousseurL, ciblePousseurC)) return null;

        while (!fileCheminPousseur.estVide()) {

            Noeud n = fileCheminPousseur.extrait();
            // System.out.println("Extrait : "+(n.pousseurL)+" "+n.pousseurC + ";   Caisse "+n.caisseL + " "+ n.caisseC);

            if (visite[n.pousseurL][n.pousseurC]){
                // System.out.println("deja visité !!! " +n.pousseurL + " "+ n.pousseurC);
                continue;
            }
            visite[n.pousseurL][n.pousseurC] = true;

            if (n.pousseurL == ciblePousseurL && n.pousseurC == ciblePousseurC) {
                // System.out.println("ciblePousseurL : "+ciblePousseurL);
                // System.out.println("ciblePousseurC : "+ciblePousseurC);
                objectif = n;

                while (objectif.prec != null) {
                    objectif = objectif.prec ;
                }
                objectif.prec = startConfig;
                return n;
            }

            ajouteVoisinPousseur(fileCheminPousseur, n, n.pousseurL + 1, n.pousseurC, ciblePousseurL, ciblePousseurC);
            ajouteVoisinPousseur(fileCheminPousseur, n, n.pousseurL - 1, n.pousseurC, ciblePousseurL, ciblePousseurC);
            ajouteVoisinPousseur(fileCheminPousseur, n, n.pousseurL, n.pousseurC + 1, ciblePousseurL, ciblePousseurC);
            ajouteVoisinPousseur(fileCheminPousseur, n, n.pousseurL, n.pousseurC - 1, ciblePousseurL, ciblePousseurC);
        }

        System.out.println("On n a pas trouve de chemin pour pousseur vers ("+ ciblePousseurL +","+ ciblePousseurC+") !!!");
        return null;
    }

    void ajouteVoisinPousseur(FAPListe<Noeud> f, Noeud parent, int l, int c, int ciblePousseurL, int ciblePousseurC) {

        if (l < 0 || c < 0) return;
        if (l >= lignes || c >= colonnes) return;

        if ((l == parent.caisseL && c == parent.caisseC) || copieSansCaisseEtPousseur.aMur(l, c)) {
            return;
        }

        f.insere(new Noeud(parent.caisseL, parent.caisseC,l,c, parent, ciblePousseurL, ciblePousseurC));
    }

    public int[][][] calcule_permutation(int[][] tabBut) {
        int n = tabBut.length;
        int nbPerm = (int) factorielle(n);
        
        // Initialisation : [combinaisons][nombre de buts][2 coordonnées]
        int[][][] res = new int[nbPerm][n][2];

        this.compteurLigne = 0;
        // .clone() sur un tableau 2D ne copie que la première couche, 
        // mais pour l'algo de Heap, c'est suffisant ici.
        generer(n, tabBut.clone(), res);

        return res;
    }

    private void generer(int n, int[][] buts, int[][][] res) {
        if (n == 1) {
            for (int j = 0; j < buts.length; j++) {
                // On remplit la 3ème dimension
                res[compteurLigne][j][0] = buts[j][0];
                res[compteurLigne][j][1] = buts[j][1];
            }
            compteurLigne++;
        } else {
            for (int i = 0; i < n; i++) {
                generer(n - 1, buts, res);
                echanger(buts, (n % 2 == 0) ? i : 0, n - 1);
            }
        }
    }

    private void echanger(int[][] t, int i, int j) {
        int[] temp = t[i];
        t[i] = t[j];
        t[j] = temp;
    }

    public long factorielle(int n) {
        long res = 1;
        for (int i = 2; i <= n; i++) res *= i;
        return res;
    }

}