package Modele;

import Global.Configuration;
import Structures.Sequence;
import Structures.FAPListe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class IADijkstra extends IA {

    int buts[][];
    int caisses[][];
    int nb_caisses;
    Niveau copie;
    int lignes, colonnes;
    int nb_comb_but_caisse = 0;
    int toutes_comb_but_caisse[][][];
    int cbc;
    boolean emplacement_mort[][][];


    IADijkstra(){
        buts = null;
        caisses = null;
    }

    class Noeud implements Comparable<Noeud> {
        int caisses[][];
        int icb;
        int pousseurL, pousseurC;
        int distance;
        Noeud prec;
        boolean caisse_range[];

        Noeud(int icb, int caisses[][], int pousseurL, int pousseurC, Noeud p, int butL, int butC, int l,int c, boolean cr[]) {
            this.caisses = caisses;
            this.icb = icb;
            this.prec = p;
            this.pousseurL = pousseurL;
            this.pousseurC = pousseurC;
            distance = Math.abs(butL-l)+Math.abs(butC-c);
            caisse_range = new boolean[nb_caisses];
            if(cr!=null){
            for(int i=0; i<nb_caisses; i++)
                caisse_range[i] = cr[i];}
        }
        @Override
        public int compareTo(Noeud n) {
            return this.distance - n.distance ;
        }
        @Override
        public String toString() {
            if(prec==null) return  "null   ->   C(" + caisses[icb][0] + ", " + caisses[icb][1] + ")   P(" + pousseurL + ", " + pousseurC + ")";
            else return  "distance : "+this.distance+" C(" + prec.caisses[icb][0] + "," + prec.caisses[icb][1] + ") P(" + prec.pousseurL + "," + prec.pousseurC + ")   ->   C(" + caisses[icb][0] + ", " + caisses[icb][1] + ") P(" + pousseurL + ", " + pousseurC + ")";
        }
    }


    public Sequence<Coup> joue() {
        lignes = niveau.lignes();
        colonnes = niveau.colonnes();

        // chercher caisses et buts
        List<int[]> caissesDin = new ArrayList<>();
        List<int[]> butsDin = new ArrayList<>();

        for (int i = 0; i < lignes; i++) {
            for (int j = 0; j < colonnes; j++) {
                if (niveau.aCaisse(i, j)) {
                    // Syntaxe correcte : new int[]{valeur1, valeur2}
                    caissesDin.add(new int[]{i, j});
                }
                if (niveau.aBut(i, j)) {
                    butsDin.add(new int[]{i, j});
                }
            }
        }

        if(caissesDin.size()!=butsDin.size()) return null;

        nb_caisses = caissesDin.size();
        caisses = new int[nb_caisses][2];
        buts = new int[nb_caisses][2];

        for (int i = 0; i < nb_caisses; i++) {
            int coordsC[] = caissesDin.get(i);
            caisses[i]= coordsC;
            int coordsB[] = butsDin.get(i);
            buts[i]= coordsB;
        }

        nb_comb_but_caisse = (int)factorielle(nb_caisses);
        // int toutes_comb_but_caisse[nb_comb_but_caisse][nb_caisses][2] = calcule_permutation(buts);
        toutes_comb_but_caisse= calcule_permutation(buts);


        for(int comb_but_caisse=0; comb_but_caisse<nb_comb_but_caisse; comb_but_caisse++){ // pour chaque combinaison possible de caisses avec des buts
            emplacement_mort = new boolean[nb_caisses][lignes][colonnes];
            cbc = comb_but_caisse;
            FAPListe<Noeud> file = new FAPListe<>();
            for(int i=0; i<nb_caisses; i++){
                file.insere(new Noeud(i, caisses, niveau.lignePousseur(), niveau.colonnePousseur(), null, toutes_comb_but_caisse[comb_but_caisse][i][0], toutes_comb_but_caisse[comb_but_caisse][i][1], caisses[i][0], caisses[i][1], new boolean[nb_caisses]));
            }

            Noeud objectif = null;
            // boolean[][] visites = new boolean[lignes][colonnes];

            while (!file.estVide()) {
                Noeud n = file.extrait();
                // System.out.println("Extrait : C("+n.caisseL+","+n.caisseC+")   P("+n.pousseurL+","+n.pousseurC+")");

                if (n.caisses[n.icb][0]==toutes_comb_but_caisse[comb_but_caisse][n.icb][0] && n.caisses[n.icb][1]==toutes_comb_but_caisse[comb_but_caisse][n.icb][1]) {
                    System.out.println("On a trouvé un chemin pour une caisse!");
                    n.caisse_range[n.icb] = true;
                    // System.out.println("caisse_range "+caisse_range[0]+"  "+caisse_range[1]);

                    

                    if(!toutesRangees(n.caisse_range)){
                        // emplacement_mort = new boolean[nb_caisses][lignes][colonnes];
                        continue;
                    }

                    objectif = n;
                    // System.out.println("objectif : "+ n.l + " " + n.c);
                    Sequence<Coup> resultat = Configuration.nouvelleSequence();
                    if(toutesRangees(n.caisse_range))System.out.println("Toutes caisses sont rangées !");
                    while (objectif.prec != null) {
                        Coup push = new Coup();
                        if((objectif.prec.pousseurL-objectif.pousseurL)!=0 || (objectif.prec.pousseurC-objectif.pousseurC)!=0)
                            push.deplacementPousseur( objectif.prec.pousseurL,  objectif.prec.pousseurC, objectif.pousseurL, objectif.pousseurC);

                        if((objectif.prec.caisses[objectif.icb][0] - objectif.caisses[objectif.icb][0]) != 0 
                        || (objectif.prec.caisses[objectif.icb][1] - objectif.caisses[objectif.icb][1]) != 0)
                            push.deplacementCaisse(objectif.prec.caisses[objectif.icb][0], objectif.prec.caisses[objectif.icb][1], objectif.caisses[objectif.icb][0], objectif.caisses[objectif.icb][1]);
                        
                            if((objectif.prec.pousseurL-objectif.pousseurL)!=0 || (objectif.prec.pousseurC-objectif.pousseurC)!=0
                            || (objectif.prec.caisses[objectif.icb][0]-objectif.prec.caisses[objectif.icb][0])!=0 
                        || (objectif.prec.caisses[objectif.icb][1]-objectif.caisses[objectif.icb][1])!=0){
                            resultat.insereTete(push);
                            System.out.println("pousseur : " + push.pousseur + "   caisse : "+ push.caisse);
                        }
                        objectif = objectif.prec;
                    }
                    return resultat;
                }
                // System.out.println("On traite : ("+n.caisseL+","+n.caisseC+")");
                    // System.out.println("caisse_range "+caisse_range[0]+"  "+caisse_range[1]);

                for(int i=0; i<nb_caisses; i++){
                    int c = 0;
                    if(!emplacement_mort[i][n.caisses[i][0]][n.caisses[i][1]]){
                        if(n.prec==null || !(n.prec.caisses[i][0]==n.caisses[i][0] + 1 && n.prec.caisses[i][1]==n.caisses[i][1]))
                            c += ajouteDeplacementCaisse(file, n, n.caisses[i][0] + 1, n.caisses[i][1], i);
                        // System.out.print("bas c="+c+" : ");
                        // System.out.println(file);
                        if(n.prec==null || !(n.prec.caisses[i][0]==n.caisses[i][0]- 1 && n.prec.caisses[i][1]==n.caisses[i][1]))
                            c += ajouteDeplacementCaisse(file, n, n.caisses[i][0] - 1, n.caisses[i][1], i);
                        // System.out.print("haut c="+c+" : ");
                        // System.out.println(file);
                        if(n.prec==null || !(n.prec.caisses[i][0]==n.caisses[i][0] && n.prec.caisses[i][1]==n.caisses[i][1]+1))
                            c += ajouteDeplacementCaisse(file, n, n.caisses[i][0], n.caisses[i][1]+1, i);
                        // System.out.print("droite c="+c+" : ");
                        // System.out.println(file);
                        if(n.prec==null || !(n.prec.caisses[i][0]==n.caisses[i][0] && n.prec.caisses[i][1]==n.caisses[i][1]-1))
                            c += ajouteDeplacementCaisse(file, n, n.caisses[i][0], n.caisses[i][1]-1, i);
                        // System.out.print("gauche c="+c+" : ");
                        // System.out.println(file);
                    }
                    if(c==0){
                        System.out.println("emplacement_mort " +i+" "+n.caisses[i][0] +" "+n.caisses[i][1]);
                        emplacement_mort[i][n.caisses[i][0]][n.caisses[i][1]]=true;
                    }
                }
                

            // try {
            //     TimeUnit.SECONDS.sleep(1); // Sleep for 1 second
            // } catch (InterruptedException e) {
            //     System.out.println("Thread was interrupted: " + e.getMessage());
            // }        
            }
            System.out.println("Pas de solution pour cette permutation de caisses et buts");
        }
        return Configuration.nouvelleSequence();
    }

    boolean toutesRangees(boolean c[]){
        for(int i=0; i<c.length; i++){
            System.out.print(c[i]+" ");
            if(c[i]==false) {
                System.out.println();
                return false;
            }
        }
        return true;
    }

    int ajouteDeplacementCaisse(FAPListe<Noeud> f, Noeud parent, int l, int c, int i) {
        if (l < 0 || c < 0 || l >= lignes || c >= colonnes)
            return 0;
        if (niveau.aMur(l, c)) {
            return 0;
        }
        for(int j=0; j<nb_caisses; j++){
            if(j!=i && l==parent.caisses[j][0] && c==parent.caisses[j][1]) return 0;
        }
        if(emplacement_mort[i][l][c]) return 0;
        // if(l==parent.pousseurL+1 && c==parent.pousseurC || 
        //     l==parent.pousseurL-1 && c==parent.pousseurC ||
        //     l==parent.pousseurL && c==parent.pousseurC+1 ||
        //     l==parent.pousseurL && c==parent.pousseurC-1){
        //     f.insere(new Noeud(l, c, parent.caisseL, parent.caisseC, parent, butL, butC));
        // }
        // else {
            Noeud finCheminPousseur = chercheCheminPousseur(f, parent, l, c, i);
            if (finCheminPousseur != null) {
                int new_caisses[][] = new int[nb_caisses][2];
                for(int k = 0; k < nb_caisses; k++){
                    new_caisses[k][0] = parent.caisses[k][0];
                    new_caisses[k][1] = parent.caisses[k][1];
                }
                new_caisses[i][0] = l;
                new_caisses[i][1] = c;
                // System.out.println(toutes_comb_but_caisse[cbc][i][0]+" "+toutes_comb_but_caisse[cbc][i][1]+" "+l+" "+c);
                boolean new_cr[] =null;
                if(!(l==toutes_comb_but_caisse[cbc][i][0] && c==toutes_comb_but_caisse[cbc][i][1])){
                     new_cr = new boolean[nb_caisses];
                    for(int j=0;j<nb_caisses;j++){
                        new_cr[i]=parent.caisse_range[i];
                        if(j==i) new_cr[i]=false;
                    }
                    
                }
                f.insere(new Noeud(i, new_caisses, parent.caisses[i][0], parent.caisses[i][1], finCheminPousseur, toutes_comb_but_caisse[cbc][i][0], toutes_comb_but_caisse[cbc][i][1], l,c, new_cr));
                
                for(int j=0; j<nb_caisses; j++){
                    if(parent.caisses[i][0]+1==parent.caisses[j][0] && parent.caisses[i][1]==parent.caisses[j][1]){
                        emplacement_mort[j][parent.caisses[i][0]+1][parent.caisses[i][1]]=false;
                    }
                    if(parent.caisses[i][0]-1==parent.caisses[j][0] && parent.caisses[i][1]==parent.caisses[j][1]){
                        emplacement_mort[j][parent.caisses[i][0]-1][parent.caisses[i][1]]=false;
                    }
                    if(parent.caisses[i][0]==parent.caisses[j][0] && parent.caisses[i][1]+1==parent.caisses[j][1]){
                        emplacement_mort[j][parent.caisses[i][0]][parent.caisses[i][1]+1]=false;
                    }
                    if(parent.caisses[i][0]==parent.caisses[j][0] && parent.caisses[i][1]-1==parent.caisses[j][1]){
                        emplacement_mort[j][parent.caisses[i][0]][parent.caisses[i][1]-1]=false;
                    }
                }
                return 1;
            }
            return 0;
        // }
    }
    
    Noeud chercheCheminPousseur(FAPListe<Noeud> f, Noeud startConfig, int cibleCaisseL, int cibleCaisseC, int i){
        boolean[][] visite = new boolean[lignes][colonnes];
        FAPListe<Noeud> fileCheminPousseur = new FAPListe<>();
        int dL = cibleCaisseL - startConfig.caisses[i][0] ;
        int dC = cibleCaisseC - startConfig.caisses[i][1] ;
        int ciblePousseurL = startConfig.caisses[i][0]-dL;
        int ciblePousseurC = startConfig.caisses[i][1]-dC;

        fileCheminPousseur.insere(new Noeud(i, startConfig.caisses, startConfig.pousseurL, startConfig.pousseurC, null, ciblePousseurL, ciblePousseurC, startConfig.pousseurL, startConfig.pousseurC, startConfig.caisse_range));

        Noeud objectif = null;
        if(niveau.aMur(ciblePousseurL, ciblePousseurC)) return null;

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
                if(objectif.prec == null){
                // System.out.println(cibleCaisseL + " "+ cibleCaisseC +"\n");
                //     int new_caisses[][] = new int[nb_caisses][2];
                //     for(int k = 0; k < nb_caisses; k++){
                //         new_caisses[k][0] = n.caisses[k][0];
                //         new_caisses[k][1] = n.caisses[k][1];
                //     }
                //     new_caisses[i][0]=cibleCaisseL;
                //     new_caisses[i][1]=cibleCaisseC;
                //     // System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
                //                     System.out.println(toutes_comb_but_caisse[cbc][i][0]+" "+toutes_comb_but_caisse[cbc][i][1]+" "+cibleCaisseL+" "+cibleCaisseC);

                //     f.insere(new Noeud(i, new_caisses, startConfig.caisses[i][0], startConfig.caisses[i][1], startConfig, toutes_comb_but_caisse[cbc][i][0], toutes_comb_but_caisse[cbc][i][1], cibleCaisseL, cibleCaisseC ));
                //     return null;
                return startConfig;
                }
                while (objectif.prec != null) {
                    objectif = objectif.prec ;
                }
                objectif.prec = startConfig;
                return n;
            }

            ajouteVoisinPousseur(fileCheminPousseur, n, n.pousseurL + 1, n.pousseurC, ciblePousseurL, ciblePousseurC, i);
            ajouteVoisinPousseur(fileCheminPousseur, n, n.pousseurL - 1, n.pousseurC, ciblePousseurL, ciblePousseurC, i);
            ajouteVoisinPousseur(fileCheminPousseur, n, n.pousseurL, n.pousseurC + 1, ciblePousseurL, ciblePousseurC, i);
            ajouteVoisinPousseur(fileCheminPousseur, n, n.pousseurL, n.pousseurC - 1, ciblePousseurL, ciblePousseurC, i);
        }

        System.out.println("On n a pas trouve de chemin pour pousseur vers ("+ ciblePousseurL +","+ ciblePousseurC+") !!!");
        return null;
    }

    void ajouteVoisinPousseur(FAPListe<Noeud> f, Noeud parent, int l, int c, int ciblePousseurL, int ciblePousseurC, int i) {

        if (l < 0 || c < 0) return;
        if (l >= lignes || c >= colonnes) return;

        if (niveau.aMur(l, c)) {
            return;
        }
        for(int j=0; j<nb_caisses; j++){
            if(l==parent.caisses[j][0] && c==parent.caisses[j][1]) return;
        }

        f.insere(new Noeud(i, parent.caisses,l,c, parent, ciblePousseurL, ciblePousseurC, l,c, parent.caisse_range));
    }

    public int[][][] calcule_permutation(int[][] tabBut) {
        int n = tabBut.length;
        int nbPerm = (int) factorielle(n);
        
        // Initialisation : [combinaisons][nombre de buts][2 coordonnées]
        int[][][] res = new int[nbPerm][n][2];

        this.nb_comb_but_caisse = 0;
        // .clone() sur un tableau 2D ne copie que la première couche, 
        // mais pour l'algo de Heap, c'est suffisant ici.
        generer(n, tabBut.clone(), res);

        return res;
    }

    private void generer(int n, int[][] buts, int[][][] res) {
        if (n == 1) {
            for (int j = 0; j < buts.length; j++) {
                // On remplit la 3ème dimension
                res[nb_comb_but_caisse][j][0] = buts[j][0];
                res[nb_comb_but_caisse][j][1] = buts[j][1];
            }
            nb_comb_but_caisse++;
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