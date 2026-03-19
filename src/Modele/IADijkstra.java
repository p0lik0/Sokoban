package Modele;

import Global.Configuration;
import Modele.IADijkstra.Noeud;
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

        class Noeud2 implements Comparable<Noeud2> {
        int l, c;
        int l2, c2;
        int distance;
        Noeud2 prec;

        Noeud2(int l, int c, int l2, int c2, Noeud2 p) {
            this.l = l;
            this.c = c;
            this.l2 = l2;
            this.c2 = c2;
            this.prec = p;

            if (p == null)
                distance = 0;
            else
                distance = p.distance + 1;
        }
        @Override
        public int compareTo(Noeud2 n) {
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
                Niveau copie2 = niveau.clone();

                //trouver la configuration initiale pour effectuer les deplacement calculés
                while (objectif != null && objectif.prec != null) {
                    int startPositionPousseurL=-1;
                    int startPositionPousseurC=-1;
                    ajouterDeplacementsTrouvesCaisse(copie, objectif.prec, objectif, resultat, startPositionPousseurL, startPositionPousseurC);
                    
                    cheminPousseur(copie2, startPositionPousseurL, startPositionPousseurC, caisseL, caisseC, resultat);
                }
                if(mauvaisChemin){
                    System.out.println("Le chemin n'était pas bon, on continue de chercher !");
                    continue;
                }
                return resultat;
            }
            System.out.println("On traite : ("+n.l+","+n.c+")");

            System.out.print("Ajoutés : ");
            if(n.l+1<lignes && copieSansCaisseEtPousseur.estOccupable(n.l-1, n.c) && visites[n.l+1][n.c]==false){
                ajouteVoisin(file, n, n.l + 1, n.c);
                System.out.print((n.l+1) + " "+ n.c + ";  ");
                visites[n.l+1][n.c]=true;
            }
            // else System.out.print((n.l-1) + " "+ n.c + " n est pas occupble\n");
            if(n.l-1>=0 && copieSansCaisseEtPousseur.estOccupable(n.l+1, n.c) && visites[n.l-1][n.c]==false){
                ajouteVoisin(file, n, n.l - 1, n.c);
                System.out.print((n.l-1) + " "+ n.c + ";  ");
                visites[n.l-1][n.c]=true;
            }
            // else System.out.print((n.l+1) + " "+ n.c + " n est pas occupble\n");
            if(n.c+1<colonnes && copieSansCaisseEtPousseur.estOccupable(n.l, n.c-1) && visites[n.l][n.c+1]==false){
                ajouteVoisin(file, n, n.l, n.c + 1);
                System.out.print((n.l) + " "+ (n.c+1) + ";  ");
                visites[n.l][n.c+1]= true;
            }
            // else System.out.print((n.l) + " "+ (n.c-1) + " n est pas occupble\n");
            if(n.c-1>=0 && copieSansCaisseEtPousseur.estOccupable(n.l, n.c+1) && visites[n.l][n.c-1]==false){
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

    void ajouterDeplacementsTrouvesCaisse(Niveau copie, Noeud nP, Noeud n, Sequence<Coup> sc, int startl, int startc){
        if(nP.prec!=null){
            startl = nP.prec.l>nP.l ? nP.prec.l+1 : (nP.prec.l<nP.l ? nP.prec.l-1 : startl);
            startc = nP.prec.c>nP.c ? nP.prec.c+1 : (nP.prec.c<nP.c ? nP.prec.c-1 : startc);
            ajouterDeplacementsTrouvesCaisse(copie, nP.prec, nP, sc, startl, startc);
        }
        Coup cp = copie.deplace(n.l - nP.l, n.c - nP.c);
        sc.insereQueue(cp);
    }

    void ajouterDeplacementsTrouvesPousseur(Niveau copie, Noeud2 nP, Noeud2 n, Sequence<Coup> sc){
        if(nP.prec!=null){
            ajouterDeplacementsTrouvesPousseur(copie, nP.prec, nP, sc);
        }
        Coup cp = copie.deplace(n.l - nP.l, n.c - nP.c);
        sc.insereQueue(cp);
    }

    Sequence<Coup> cheminPousseur(Niveau copie, int ciblePousseurL, int ciblePousseurC, int cibleCaisseL, int cibleCaisseC, Sequence<Coup> chemin) {
        boolean[][] visite = new boolean[lignes][colonnes];
        FAPListe<Noeud2> file = new FAPListe<>();
        System.out.println("caisseL et C: "+caisseL + " " + caisseC);

        file.insere(new Noeud2(copie.lignePousseur(), copie.colonnePousseur(), caisseL, caisseC, null));
        Noeud2 objectif = null;

        while (!file.estVide()) {

            Noeud2 n = file.extrait();
            System.out.println("Extrait :" + n.l +" "+ n.c + " & " + (n.l2) + " " + n.c2 + ";  ");

            if (visite[n.l][n.c])
                continue;

            visite[n.l][n.c] = true;

            if (n.l==ciblePousseurL && n.c==ciblePousseurC && n.l2==cibleCaisseL && n.c2==cibleCaisseC) {
                objectif = n;
                break;
            }
            System.out.print("Ajoutés : ");
            System.out.print("(n.l+1) : "+((n.l+1)<lignes));
            if((n.l+1)<lignes && (copieSansCaisseEtPousseur.estOccupable(n.l + 1, n.c) || copieSansCaisseEtPousseur.aCaisse(n.l+1, n.c))){
                ajouteVoisinPousseur(copieSansCaisseEtPousseur, file, n, n.l + 1, n.c, n.l2, n.c2, 2);
                System.out.println((n.l+1) + " " + n.c + " & " +n.l2 + " " +n.c2 + " (caisse non bougé)");
            }
            if((n.l-1)>=0 && (copieSansCaisseEtPousseur.estOccupable(n.l - 1, n.c)  || copieSansCaisseEtPousseur.aCaisse(n.l-1, n.c))){
                ajouteVoisinPousseur(copieSansCaisseEtPousseur, file, n, n.l - 1, n.c, n.l2, n.c2, 0);
                System.out.println((n.l-1) + " " + n.c + " & " +n.l2 + " " +n.c2 + " (caisse non bougé)");
            }
            if((n.c+1)<colonnes && (copieSansCaisseEtPousseur.estOccupable(n.l, n.c+ 1)  || copieSansCaisseEtPousseur.aCaisse(n.l, n.c+1))){
                ajouteVoisinPousseur(copieSansCaisseEtPousseur, file, n, n.l, n.c + 1, n.l2, n.c2, 1);
                System.out.println(n.l + " " + (n.c+1) + " & " +n.l2 + " " +n.c2 + " (caisse non bougé)");
            }
            if((n.c-1)>=0 && (copieSansCaisseEtPousseur.estOccupable(n.l, n.c - 1)  || copieSansCaisseEtPousseur.aCaisse(n.l, n.c-1))){
                ajouteVoisinPousseur(copieSansCaisseEtPousseur, file, n, n.l, n.c - 1, n.l2, n.c2, 3);
                System.out.println(n.l + " " + (n.c-1) + " & " +n.l2 + " " +n.c2 + " (caisse non bougé)");
            }
            System.out.println();
        }

        if (objectif == null)
            return null;

        ajouterDeplacementsTrouvesPousseur(copie, objectif.prec, objectif, chemin);

        return chemin;
    }
    void ajouteVoisinPousseur(Niveau copie, FAPListe<Noeud2> f, Noeud2 parent, int l, int c, int l2, int c2, int dir) {
        if (l < 0 || c < 0)
            return;

        if (l >= lignes || c >= colonnes)
            return;
        
        switch(dir){
            case 0:
                if(l==l2 && (!copie.estOccupable(l2-1, c2) || l2<1)) return;
                else{
                    if(l==l2 && c==c2){
                        f.insere(new Noeud2(l, c, l2-1, c2, parent));
                        System.out.print(l +" "+ c + " & " + (l2-1) + " " + c2 + ";  ");
                        return;
                    }
                }
                break;
            case 1:
                if( l==l2 && (!copie.estOccupable(l2, c2+1) || c2>=colonnes-1)) return;
                else{
                    if(l==l2 && c==c2){
                        f.insere(new Noeud2(l, c, l2, c2+1, parent));
                        System.out.print(l +" "+ c + " & " + (l2) + " " + (c2+1) + ";  ");
                        return;
                    }
                }
                break;
            case 2:
                if(l==l2 && (!copie.estOccupable(l2+1, c2) || l2>=lignes-1)) return;
                else{
                    if(l==l2 && c==c2){
                        f.insere(new Noeud2(l, c, l2+1, c2, parent));
                        System.out.print(l +" "+ c + " & " + (l2+1) + " " + c2 + ";  ");
                        return;
                    }
                }
                break;
            case 3:
                if(c==c2 && (!copie.estOccupable(l2, c2-1) || c2<1)) return;
                else{
                    if(l==l2 && c==c2){
                        f.insere(new Noeud2(l, c, l2, c2-1, parent));
                        System.out.print(l +" "+ c + " & " + (l2) + " " + (c2-1) + ";  ");
                        return;
                    }
                }
                break;
        }
        System.out.print(l +" "+ c + " & " + (l2) + " " + c2 + ";  ");
        f.insere(new Noeud2(l, c, l2, c2, parent));
    }
}