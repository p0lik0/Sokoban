package Modele;

import Global.Configuration;
import Structures.Sequence;
import Structures.FAPListe;

public class IADijkstra extends IA {

    int butL, butC;
    int caisseL, caisseC;
    Niveau copieSansCaisseEtPousseur;
    int lignes, colonnes;

    IADijkstra(){
        butL = -1;
        butC = -1;
        caisseL = -1;
        caisseC = -1;
    }

    class Noeud implements Comparable<Noeud> {
        int l, c;
        int distance;
        Noeud prec;

        Noeud(int l, int c, Noeud p) {
            this.l = l;
            this.c = c;
            this.prec = p;

            if (p == null)
                distance = 0;
            else
                distance = p.distance + 1;
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
        file.insere(new Noeud(caisseL, caisseC, null));
        Noeud objectif = null;
        boolean[][] visites = new boolean[lignes][colonnes];

        while (!file.estVide()) {
            Noeud n = file.extrait();

            if (n.l == butL && n.c == butC) {
                System.out.println("On a trouvé un chemin !");
                objectif = n;
                // System.out.println("objectif : "+ n.l + " " + n.c);
                boolean mauvaisChemin = false;
                Sequence<Coup> resultat = Configuration.nouvelleSequence();
                Niveau copie = niveau.clone();
                int pousseurL = -1;
                int pousseurC = -1;

                while (objectif.prec != null) {
                    
                    int dL = objectif.l - objectif.prec.l;
                    int dC = objectif.c - objectif.prec.c;

                    if(objectif.prec.prec == null){
                        pousseurL = niveau.lignePousseur();
                        pousseurC = niveau.colonnePousseur();
                    }
                    else{
                        pousseurL = objectif.prec.prec.l;
                        pousseurC = objectif.prec.prec.c;
                    }
                    caisseL = objectif.prec.l;
                    caisseC = objectif.prec.c;

                    // 📍 куда должен прийти pousseur
                    int cibleL = objectif.prec.l - dL;
                    int cibleC = objectif.prec.c - dC;

                    // 1. ищем путь pousseur
                    System.out.println("aAAAAAAAAAAAAAAAaaaaaaaaAAAAAa "+  cibleL +" "+cibleC);
                    Sequence<Coup> cheminP = cheminPousseur(cibleL, cibleC, pousseurL, pousseurC);

                    if (cheminP == null) {
                        mauvaisChemin = true;
                        break;
                    }

                    Coup push = new Coup();
                    push.deplacementPousseur(cibleL, cibleC, objectif.prec.l, objectif.prec.c);
                    push.deplacementCaisse(objectif.prec.l, objectif.prec.c, objectif.l, objectif.c);
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
            System.out.println("On traite : ("+n.l+","+n.c+")");

            System.out.print("Ajoutés : ");
            if( n.l-1>=0 && copieSansCaisseEtPousseur.estOccupable(n.l-1, n.c) && visites[n.l+1][n.c]==false){
                ajouteVoisin(file, n, n.l + 1, n.c);
                System.out.print((n.l+1) + " "+ n.c + ";  ");
                visites[n.l+1][n.c]=true;
            }
            // else System.out.print((n.l-1) + " "+ n.c + " n est pas occupble\n");
            if(n.l+1<lignes && copieSansCaisseEtPousseur.estOccupable(n.l+1, n.c) && visites[n.l-1][n.c]==false){
                ajouteVoisin(file, n, n.l - 1, n.c);
                System.out.print((n.l-1) + " "+ n.c + ";  ");
                visites[n.l-1][n.c]=true;
            }
            // else System.out.print((n.l+1) + " "+ n.c + " n est pas occupble\n");
            if( n.c-1>=0 && copieSansCaisseEtPousseur.estOccupable(n.l, n.c-1) && visites[n.l][n.c+1]==false){
                ajouteVoisin(file, n, n.l, n.c + 1);
                System.out.print((n.l) + " "+ (n.c+1) + ";  ");
                visites[n.l][n.c+1]= true;
            }
            // else System.out.print((n.l) + " "+ (n.c-1) + " n est pas occupble\n");
            if( n.c+1<colonnes && copieSansCaisseEtPousseur.estOccupable(n.l, n.c+1) && visites[n.l][n.c-1]==false){
                ajouteVoisin(file, n, n.l, n.c-1);
                System.out.println((n.l) + " "+ (n.c-1) + ";  ");
                visites[n.l][n.c-1]=true;
            }
            // else System.out.print((n.l) + " "+ (n.c+1) + " n est pas occupble\n");
            System.out.println();
        }
        return Configuration.nouvelleSequence();
    }

    void ajouteVoisin(FAPListe<Noeud> f, Noeud parent, int l, int c) {

        if (l < 0 || c < 0)
            return;

        if (l >= lignes || c >= colonnes)
            return;

        if (copieSansCaisseEtPousseur.estOccupable(l, c) || copieSansCaisseEtPousseur.aBut(l, c)) {
            f.insere(new Noeud(l, c, parent));
        }
    }

    Sequence<Coup> cheminPousseur(int cibleL, int cibleC, int pousseurL, int pousseurC) {

        boolean[][] visite = new boolean[lignes][colonnes];
        FAPListe<Noeud> file = new FAPListe<>();

        file.insere(new Noeud(pousseurL, pousseurC, null));

        Noeud objectif = null;

        while (!file.estVide()) {

            Noeud n = file.extrait();
            // System.out.println("Extrait : "+(n.l)+" "+n.c + "; ");

            if (visite[n.l][n.c])
                continue;

            visite[n.l][n.c] = true;

            if (n.l == cibleL && n.c == cibleC) {
                objectif = n;
                break;
            }

            ajouteVoisinPousseur(file, n, n.l + 1, n.c);
            // System.out.print((n.l+1)+" "+n.c + "; ");
            ajouteVoisinPousseur(file, n, n.l - 1, n.c);
            // System.out.print((n.l-1)+" "+n.c + "; ");
            ajouteVoisinPousseur(file, n, n.l, n.c + 1);
            // System.out.print(n.l+" "+(n.c+1)+ "; ");
            ajouteVoisinPousseur(file, n, n.l, n.c - 1);
            // System.out.println((n.l)+" "+(n.c-1) + "; ");
        }

        if (objectif == null){
            System.out.println("On n a pas trouve de chemin pour pousseur vers ("+ cibleL +","+ cibleC+") !!!");
            return null;
        }
        Sequence<Coup> chemin = Configuration.nouvelleSequence();

        while (objectif.prec != null) {
            Coup cp = new Coup();
            cp.deplacementPousseur(objectif.prec.l, objectif.prec.c, objectif.l, objectif.c);
            chemin.insereQueue(cp);
            objectif = objectif.prec;
        }

        return chemin;
    }
    void ajouteVoisinPousseur(FAPListe<Noeud> f, Noeud parent, int l, int c) {

        if (l < 0 || c < 0)
            return;

        if (l >= lignes || c >= colonnes)
            return;

        // ❗ pousseur не проходит через ящик и стены
        if (!copieSansCaisseEtPousseur.aMur(l, c) && !(l==caisseL && c==caisseC)) {
            // System.out.println("Ajouté : "+l + " " + c);
            f.insere(new Noeud(l, c, parent));
        }
        // else{
        //     System.out.println("\n NON : "+l + " " + c);
        // }
    }
}