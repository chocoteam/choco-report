package choco.ui;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class BugsComponent extends CustomComponent {

    // Récupération des identifiants pour se connecter à la BDD
    Connection cn = null;
    Statement st = null;
    ResultSet rs = null;
    // Récupération des noms des problèmes pour les BENCHMARKS 1 et 2 et les
    // trois types de bugs
    List<String> nomProblemes11 = new ArrayList<String>();
    List<String> nomProblemes12 = new ArrayList<String>();
    List<String> nomProblemes21 = new ArrayList<String>();
    List<String> nomProblemes22 = new ArrayList<String>();
    List<String> nomProblemes31 = new ArrayList<String>();
    List<String> nomProblemes32 = new ArrayList<String>();
    // Nombres pour l'onglet récapitulatif
    Integer nombreTotalBugsBenchmark1 = 0;
    Integer nombreTotalBugsBenchmark2 = 0;
    Integer nombreBugs1 = 0;
    Integer nombreBugs2 = 0;
    Integer nombreBugs3 = 0;

    public BugsComponent(String benchmark1, String benchmark2, Properties mysql) {

        try {
            String url = mysql.getProperty("mysql.url");
            String dbname = mysql.getProperty("mysql.dbname");
            String user = mysql.getProperty("mysql.user");
            String pwd = mysql.getProperty("mysql.pwd");
            // Connexion à la BDD
            Class.forName("com.mysql.jdbc.Driver");

            cn = DriverManager.getConnection("jdbc:mysql://" + url + "/" + dbname, user, pwd);
            st = cn.createStatement();

            // Requête pour récupérer les deux benchmarks des combobox
            String requetebid1 = "SELECT bid FROM BENCHMARKS WHERE name = '"
                    + benchmark1 + "'";
            rs = st.executeQuery(requetebid1);
            rs.next();
            Integer BID1 = rs.getInt("bid");

            String requetebid2 = "SELECT bid FROM BENCHMARKS WHERE name = '"
                    + benchmark2 + "'";
            rs = st.executeQuery(requetebid2);
            rs.next();
            Integer BID2 = rs.getInt("bid");

            // Requête Solving_time < 900000 et pas de solution pour récupérer
            // le nom du problème avec le benchmark 2
            String nomProbleme12 = "Select p.name FROM PROBLEMS p, RESOLUTIONS r WHERE bid = "
                    + BID2
                    + " AND r.solving_time<900000 AND r.nb_sol=0 AND r.pid=p.pid";
            rs = st.executeQuery(nomProbleme12);
            while (rs.next()) {
                nomProblemes12.add(rs.getString("name"));
            }

            // Requête Solving_time < 900000 et pas de solution pour récupérer
            // le nom du problème avec le benchmark 1
            String nomProbleme11 = "Select p.name FROM PROBLEMS p, RESOLUTIONS r WHERE bid = "
                    + BID1
                    + " AND r.solving_time<900000 AND r.nb_sol=0 AND r.pid=p.pid";
            rs = st.executeQuery(nomProbleme11);
            while (rs.next()) {
                nomProblemes11.add(rs.getString("name"));
            }

            // Requête valeur exceptionnelle
            Integer BID = 0;
            List<Integer> pids = new ArrayList<Integer>();
            // On récupère tous les IDs des problèmes
            String reqNbPbs = "SELECT pid FROM PROBLEMS";
            rs = st.executeQuery(reqNbPbs);
            while (rs.next()) {
                pids.add(rs.getInt("pid"));
            }
            // On parcourt tous les IDs des problèmes
            for (Integer numPID : pids) {
                Integer moyenne = 0;
                // On récupère les "objectives" de chaque ID
                String recupObjectiveTsPbs = "SELECT * FROM RESOLUTIONS WHERE pid = "
                        + numPID;
                rs = st.executeQuery(recupObjectiveTsPbs);
                while (rs.next()) {
                    // On fait la moyenne de tous les "objectives" pour tous les
                    // problèmes d'un benchmark
                    Integer objective = rs.getInt("objective");
                    BID = rs.getInt("bid");
                    moyenne = moyenne + objective;
                }
                moyenne = moyenne / pids.size();
                // On récupère l'objective d'un problème
                String objectivePb = "SELECT * from PROBLEMS WHERE pid = "
                        + numPID;
                rs = st.executeQuery(objectivePb);
                rs.next();
                Integer objective = rs.getInt("objective");
                Double valeur = (double) (objective - moyenne);
                if (valeur < 0) {
                    valeur = Math.abs(valeur);
                    // On le compare avec la moyenne pour savoir si ça varie de
                    // 30% ou non
                    if (moyenne * 0.3 >= valeur) {
                        String nomProbleme = rs.getString("name");
                        String requeteBID = "SELECT * FROM BENCHMARKS where bid ="
                                + BID;
                        rs = st.executeQuery(requeteBID);
                        rs.next();
                        String nomBenchmark = rs.getString("name");
                        if (benchmark1.equals(nomBenchmark)) {
                            nomProblemes21.add(nomProbleme);
                        } else {
                            if (benchmark2.equals(nomBenchmark)) {
                                nomProblemes22.add(nomProbleme);
                            }
                        }
                    }
                } else {
                    // On le compare avec la moyenne pour savoir si ça varie de
                    // 30% ou non
                    if (objective * 0.3 >= valeur) {
                        String nomProbleme = rs.getString("name");
                        String requeteBID = "SELECT * FROM BENCHMARKS where bid ="
                                + BID;
                        rs = st.executeQuery(requeteBID);
                        rs.next();
                        String nomBenchmark = rs.getString("name");
                        if (benchmark1.equals(nomBenchmark)) {
                            nomProblemes21.add(nomProbleme);
                        } else {
                            if (benchmark2.equals(nomBenchmark)) {
                                nomProblemes22.add(nomProbleme);
                            }
                        }
                    }
                }
            }

            // Requête Solving_time < 900000 et pas la meilleure solution pour
            // récupérer le nom du problème avec le benchmark 1
            String nomProbleme31 = "SELECT p.name FROM PROBLEMS p, RESOLUTIONS r WHERE bid = "
                    + BID1
                    + " AND r.solving_time<900000 AND r.pid=p.pid AND nb_sol<> 0 AND p.objective<>r.objective ";
            rs = st.executeQuery(nomProbleme31);
            while (rs.next()) {
                nomProblemes31.add(rs.getString("name"));
            }

            // Requête Solving_time < 900000 et pas la meilleure solution pour
            // récupérer le nom du problème avec le benchmark 2
            String nomProbleme32 = "SELECT p.name FROM PROBLEMS p, RESOLUTIONS r WHERE bid = "
                    + BID2
                    + " AND r.solving_time<900000 AND r.pid=p.pid AND nb_sol<> 0 AND p.objective<>r.objective";
            rs = st.executeQuery(nomProbleme32);
            while (rs.next()) {
                nomProblemes32.add(rs.getString("name"));
            }

        } catch (SQLException se) {
            se.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                cn.close();
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        final VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setMargin(true);

        // Création du tableau
        final Table table = new Table("Bugs");
        table.setSortDisabled(true);
        table.setSizeFull();
        table.setHeight("600px");
        table.addContainerProperty("Type du bug", String.class, null);
        table.addContainerProperty("Nom du Benchmark", String.class, null);
        table.addContainerProperty("Nom du Probl�me", String.class, null);

        // Remplissage de la première partie du tableau
        table.addItem(new Object[]{
                        "Temps de résolution < 900000 et pas de solution", "", ""},
                new Integer(0));
        if (nomProblemes11.equals(null) || nomProblemes11.size() == 0
                && nomProblemes12.equals(null) || nomProblemes12.size() == 0) {
            table.addItem(
                    new Object[]{"", "Aucun résultat", "Aucun résultat"},
                    new Integer(1));
        } else {
            for (int i = 1; i < 1 + nomProblemes11.size(); i++) {
                table.addItem(
                        new Object[]{"", benchmark1,
                                nomProblemes11.get(i - 1)}, new Integer(i));
            }
            for (int i = 500; i < 500 + nomProblemes12.size(); i++) {
                table.addItem(
                        new Object[]{"", benchmark2,
                                nomProblemes12.get(i - 500)}, new Integer(i));
            }
            nombreTotalBugsBenchmark1 = nombreTotalBugsBenchmark1
                    + nomProblemes11.size();
            nombreTotalBugsBenchmark2 = nombreTotalBugsBenchmark2
                    + nomProblemes12.size();
            nombreBugs1 = nomProblemes11.size() + nomProblemes12.size();
        }

        // Remplissage de la deuxième partie du tableau
        table.addItem(new Object[]{"Valeurs exceptionnelles", "", ""},
                new Integer(1000));
        if (nomProblemes21.equals(null) || nomProblemes21.size() == 0
                && nomProblemes22.equals(null) || nomProblemes22.size() == 0) {
            table.addItem(
                    new Object[]{"", "Aucun résultat", "Aucun résultat"},
                    new Integer(1001));
        } else {
            for (int i = 1001; i < 1001 + nomProblemes21.size(); i++) {
                table.addItem(
                        new Object[]{"", benchmark1,
                                nomProblemes21.get(i - 1001)}, new Integer(i));
            }
            for (int i = 1500; i < 1500 + nomProblemes22.size(); i++) {
                table.addItem(
                        new Object[]{"", benchmark2,
                                nomProblemes22.get(i - 1500)}, new Integer(i));
            }
            nombreTotalBugsBenchmark1 = nombreTotalBugsBenchmark1
                    + nomProblemes21.size();
            nombreTotalBugsBenchmark2 = nombreTotalBugsBenchmark2
                    + nomProblemes22.size();
            nombreBugs2 = nomProblemes21.size() + nomProblemes22.size();
        }

        // Remplissage de la troisième partie du tableau
        table.addItem(new Object[]{
                "Temps de résolution < 900000 et pas la meilleure solution",
                "", ""}, new Integer(2000));
        if (nomProblemes31.equals(null) || nomProblemes31.size() == 0
                && nomProblemes32.equals(null) || nomProblemes32.size() == 0) {
            table.addItem(
                    new Object[]{"", "Aucun résultat", "Aucun résultat"},
                    new Integer(2001));
        } else {
            for (int i = 2001; i < 2001 + nomProblemes31.size(); i++) {
                table.addItem(
                        new Object[]{"", benchmark1,
                                nomProblemes31.get(i - 2001)}, new Integer(i));
            }
            for (int i = 2500; i < 2500 + nomProblemes32.size(); i++) {
                table.addItem(
                        new Object[]{"", benchmark2,
                                nomProblemes32.get(i - 2500)}, new Integer(i));
            }
            nombreTotalBugsBenchmark1 = nombreTotalBugsBenchmark1
                    + nomProblemes31.size();
            nombreTotalBugsBenchmark2 = nombreTotalBugsBenchmark2
                    + nomProblemes32.size();
            nombreBugs3 = nomProblemes31.size() + nomProblemes32.size();
        }

        layout.addComponent(table);

        setCompositionRoot(layout);
    }

    public Integer getNombreTotalBugsBenchmark1() {
        return nombreTotalBugsBenchmark1;
    }

    public void setNombreTotalBugsBenchmark1(Integer nombreTotalBugsBenchmark1) {
        this.nombreTotalBugsBenchmark1 = nombreTotalBugsBenchmark1;
    }

    public Integer getNombreTotalBugsBenchmark2() {
        return nombreTotalBugsBenchmark2;
    }

    public void setNombreTotalBugsBenchmark2(Integer nombreTotalBugsBenchmark2) {
        this.nombreTotalBugsBenchmark2 = nombreTotalBugsBenchmark2;
    }

    public Integer getNombreBugs1() {
        return nombreBugs1;
    }

    public void setNombreBugs1(Integer nombreBugs1) {
        this.nombreBugs1 = nombreBugs1;
    }

    public Integer getNombreBugs2() {
        return nombreBugs2;
    }

    public void setNombreBugs2(Integer nombreBugs2) {
        this.nombreBugs2 = nombreBugs2;
    }

    public Integer getNombreBugs3() {
        return nombreBugs3;
    }

    public void setNombreBugs3(Integer nombreBugs3) {
        this.nombreBugs3 = nombreBugs3;
    }


}
