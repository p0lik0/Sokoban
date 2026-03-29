package Modele;

import java.util.HashSet;

import Global.Configuration;
import Structures.Sequence;
import Structures.FAPListe;

public class IADikstra_old extends IA {

    int butL, butC;
    int caisseL, caisseC;
    Niveau copieSansCaisseEtPousseur;
    Niveau copie;
    int lignes, colonnes;

    IADikstra_old(){
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

        Noeud(int caisseL, int caisseC, int pousseurL, int pousseurC, Noeud p) {
            this.caisseL = caisseL;
            this.caisseC = caisseC;
            this.prec = p;
            this.pousseurL = pousseurL;
            this.pousseurC = pousseurC;
            distance = 1;

            // if (p == null)
            //     distance = 0;
            // else
            //     distance = p.distance + 1;
        }
        @Override
        public int compareTo(Noeud n) {
            // return this.distance - n.distance;
            return 1;
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
        file.insere(new Noeud(caisseL, caisseC,-1, -1, null));
        Noeud objectif = null;
        // boolean[][] visites = new boolean[lignes][colonnes];

        while (!file.estVide()) {
            Noeud n = file.extrait();

            if (n.caisseL == butL && n.caisseC == butC) {
                System.out.println("On a trouvé un chemin !");
                objectif = n;
                // System.out.println("objectif : "+ n.l + " " + n.c);
                boolean mauvaisChemin = false;
                Sequence<Coup> resultat = Configuration.nouvelleSequence();
                int pousseurL = -1;
                int pousseurC = -1;

                while (objectif.prec != null) {
                    
                    int dL = objectif.caisseL - objectif.prec.caisseL;
                    int dC = objectif.caisseC - objectif.prec.caisseC;

                    if(objectif.prec.prec == null){
                        pousseurL = niveau.lignePousseur();
                        pousseurC = niveau.colonnePousseur();
                    }
                    else{
                        pousseurL = objectif.prec.prec.caisseL;
                        pousseurC = objectif.prec.prec.caisseC;
                    }
                    caisseL = objectif.prec.caisseL;
                    caisseC = objectif.prec.caisseC;

                    // 📍 куда должен прийти pousseur
                    int cibleL = objectif.prec.caisseL - dL;
                    int cibleC = objectif.prec.caisseC - dC;

                    // 1. ищем путь pousseur
                    // System.out.println("cheminPousseur(caisseL, caisseC, pousseurL, pousseurC, cibleL, cibleC) "+caisseL +" " +caisseC+" " +pousseurL+" " +pousseurC+" " +cibleL+" " +cibleC);
                    Sequence<Coup> cheminP = cheminPousseur(caisseL, caisseC, pousseurL, pousseurC, cibleL, cibleC);

                    if (cheminP == null) {
                        mauvaisChemin = true;
                        break;
                    }

                    Coup push = new Coup();
                    push.deplacementPousseur(cibleL, cibleC, objectif.prec.caisseL, objectif.prec.caisseC);
                    push.deplacementCaisse(objectif.prec.caisseL, objectif.prec.caisseC, objectif.caisseL, objectif.caisseC);
                    System.out.println("PUSH        pousseur : " + push.pousseur + "   caisse : "+ push.caisse);
                    resultat.insereTete(push);

                    while (!cheminP.estVide()) {
                        Coup cp = cheminP.extraitTete();
                        resultat.insereTete(cp);
                        System.out.println("pousseur : " + cp.pousseur + "   caisse : "+ cp.caisse);
                    }

                    objectif = objectif.prec;
                }
                if(mauvaisChemin){
                    System.out.println("Le chemin n'était pas bon, on continue de chercher !");
                    continue;
                }
                return resultat;
            }
            // System.out.println("On traite : ("+n.caisseL+","+n.caisseC+")");

            // System.out.print("Ajoutés : ");
            // if( n.caisseL-1>=0 && copieSansCaisseEtPousseur.estOccupable(n.caisseL-1, n.caisseC) && visites[n.caisseL+1][n.caisseC]==false){
            if( n.caisseL-1>=0 && copieSansCaisseEtPousseur.estOccupable(n.caisseL-1, n.caisseC)){
                ajouteVoisin(file, n, n.caisseL + 1, n.caisseC);
                // System.out.print((n.l+1) + " "+ n.c + ";  ");
                // visites[n.caisseL+1][n.caisseC]=true;
            }
            // else System.out.print((n.l-1) + " "+ n.c + " n est pas occupble\n");
            // if(n.caisseL+1<lignes && copieSansCaisseEtPousseur.estOccupable(n.caisseL+1, n.caisseC) && visites[n.caisseL-1][n.caisseC]==false){
            if(n.caisseL+1<lignes && copieSansCaisseEtPousseur.estOccupable(n.caisseL+1, n.caisseC)){
                ajouteVoisin(file, n, n.caisseL - 1, n.caisseC);
                // System.out.print((n.l-1) + " "+ n.c + ";  ");
                // visites[n.caisseL-1][n.caisseC]=true;
            }
            // else System.out.print((n.l+1) + " "+ n.c + " n est pas occupble\n");
            // if( n.caisseC-1>=0 && copieSansCaisseEtPousseur.estOccupable(n.caisseL, n.caisseC-1) && visites[n.caisseL][n.caisseC+1]==false){
            if( n.caisseC-1>=0 && copieSansCaisseEtPousseur.estOccupable(n.caisseL, n.caisseC-1)){

                ajouteVoisin(file, n, n.caisseL, n.caisseC + 1);
                // System.out.print((n.l) + " "+ (n.c+1) + ";  ");
                // visites[n.caisseL][n.caisseC+1]= true;
            }
            // else System.out.print((n.l) + " "+ (n.c-1) + " n est pas occupble\n");
            // if( n.caisseC+1<colonnes && copieSansCaisseEtPousseur.estOccupable(n.caisseL, n.caisseC+1) && visites[n.caisseL][n.caisseC-1]==false){
            if( n.caisseC+1<colonnes && copieSansCaisseEtPousseur.estOccupable(n.caisseL, n.caisseC+1)){

                ajouteVoisin(file, n, n.caisseL, n.caisseC-1);
                // System.out.println((n.l) + " "+ (n.c-1) + ";  ");
                // visites[n.caisseL][n.caisseC-1]=true;
            }
            // else System.out.print((n.l) + " "+ (n.c+1) + " n est pas occupble\n");
            // System.out.println();
        }
        return Configuration.nouvelleSequence();
    }

    void ajouteVoisin(FAPListe<Noeud> f, Noeud parent, int l, int c) {

        if (l < 0 || c < 0)
            return;

        if (l >= lignes || c >= colonnes)
            return;

        if (copieSansCaisseEtPousseur.estOccupable(l, c) || copieSansCaisseEtPousseur.aBut(l, c)) {
            f.insere(new Noeud(l, c, -1, -1, parent));
        }
    }

    Sequence<Coup> cheminPousseur(int cibleCaisseL, int cibleCaisseC, int pousseurL, int pousseurC, int ciblePousseurL, int ciblePousseurC) {
        boolean[][] visite = new boolean[lignes][colonnes];
        FAPListe<Noeud> file = new FAPListe<>();

        file.insere(new Noeud(caisseL, caisseC, pousseurL, pousseurC, null));

        Noeud objectif = null;

        while (!file.estVide()) {

            Noeud n = file.extrait();
            // System.out.println("Extrait : "+(n.pousseurL)+" "+n.pousseurC + ";   Caisse "+n.caisseL + " "+ n.caisseC);

            if (visite[n.pousseurL][n.pousseurC]){
                // System.out.println("deja visité !!! " +n.pousseurL + " "+ n.pousseurC);
                continue;
            }
            visite[n.pousseurL][n.pousseurC] = true;

            if (n.pousseurL == ciblePousseurL && n.pousseurC == ciblePousseurC && n.caisseL==cibleCaisseL && n.caisseC==cibleCaisseC) {
                objectif = n;

                Sequence<Coup> chemin = Configuration.nouvelleSequence();
                if(objectif.prec==null) return chemin;
                if(ramasseLaChaineDesCoups(objectif.prec, objectif, chemin)==null){
                    return null;
                }
                return chemin;
            }

            ajouteVoisinPousseur(file, n, n.pousseurL + 1, n.pousseurC);
            ajouteVoisinPousseur(file, n, n.pousseurL - 1, n.pousseurC);
            ajouteVoisinPousseur(file, n, n.pousseurL, n.pousseurC + 1);
            ajouteVoisinPousseur(file, n, n.pousseurL, n.pousseurC - 1);
        }

        System.out.println("On n a pas trouve de chemin pour pousseur vers ("+ ciblePousseurL +","+ ciblePousseurC+") !!!");
        return null;
    }

    int[] ramasseLaChaineDesCoups(Noeud from, Noeud to, Sequence<Coup> chemin){

        if(from == null) return null;

        // рекурсивно идём до начала
        if(from.prec != null){
            ramasseLaChaineDesCoups(from.prec, from, chemin);
        }

        // считаем ТЕКУЩЕЕ смещение
        int dL = to.pousseurL - from.pousseurL;
        int dC = to.pousseurC - from.pousseurC;

        Coup cp = new Coup();
        cp.deplacementPousseur(from.pousseurL, from.pousseurC, to.pousseurL, to.pousseurC);

        if(cp != null){
            // System.out.println("PAS POUSSEUR   pousseur : " + cp.pousseur + "   caisse : "+ cp.caisse);
            chemin.insereTete(cp);
        } 
        // else {
        //     System.out.println("Erreur dans un parcours trouvé!");
        //     return null;
        // }

        int[] res = new int[2];
        res[0] = dL;
        res[1] = dC;
        return res;
    }

    void ajouteVoisinPousseur(FAPListe<Noeud> f, Noeud parent, int l, int c) {

        if (l < 0 || c < 0) return;
        if (l >= lignes || c >= colonnes) return;

        if ((l == parent.caisseL && c == parent.caisseC) || copieSansCaisseEtPousseur.aMur(l, c)) {
            return;
        }

        f.insere(new Noeud(parent.caisseL, parent.caisseC,l,c, parent));
    }
}