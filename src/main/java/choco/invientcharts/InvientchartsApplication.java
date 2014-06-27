package choco.invientcharts;

import choco.ui.BugsComponent;
import choco.ui.PerfsComponent;
import choco.ui.RecapComponent;
import com.vaadin.Application;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.*;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Main application class.
 */
@SuppressWarnings("serial")
public class InvientchartsApplication extends Application {

    // Récupération des identifiants pour se connecter à la BDD
    Connection cn = null;
    Statement st = null;
    ResultSet rs = null;
    // Liste récupérant tous les benchmarks
    List<String> nomBenchmarks;
    // Benchmarks des deux onglets
    String benchmark1, benchmark2;


    public String getBenchmark1() {
        return benchmark1;
    }

    public void setBenchmark1(String benchmark1) {
        this.benchmark1 = benchmark1;
    }

    public String getBenchmark2() {
        return benchmark2;
    }

    public void setBenchmark2(String benchmark2) {
        this.benchmark2 = benchmark2;
    }

    @Override
    public void init() {
        try {
            InputStream inputStream = this.getClass().getClassLoader()
                    .getResourceAsStream("mysql.properties");
            final Properties properties = new Properties();
            properties.load(inputStream);

            String url = properties.getProperty("mysql.url");
            String dbname = properties.getProperty("mysql.dbname");
            String user = properties.getProperty("mysql.user");
            String pwd = properties.getProperty("mysql.pwd");
            // Connexion à la BDD
            Class.forName("com.mysql.jdbc.Driver");

            cn = DriverManager.getConnection("jdbc:mysql://" + url + "/" + dbname, user, pwd);
            st = cn.createStatement();

            // Requête pour remplir les combobox en récupérant tous les
            // benchmarks
            String nomBenchmark = "SELECT name FROM BENCHMARKS";
            rs = st.executeQuery(nomBenchmark);
            nomBenchmarks = new ArrayList<String>();
            while (rs.next()) {
                nomBenchmarks.add(rs.getString("name"));
            }

            // Requête pour récupérer les deux derniers benchmarks et les mettre
            // par défaut dans les combobox
            String bid = "SELECT MAX(bid) as bid FROM BENCHMARKS";
            rs = st.executeQuery(bid);
            rs.next();
            Integer BID2 = rs.getInt("bid");
            Integer BID1 = BID2 - 1;
            bid = "SELECT name FROM BENCHMARKS WHERE bid = " + BID1;
            rs = st.executeQuery(bid);
            rs.next();
            benchmark1 = rs.getString("name");
            bid = "SELECT name FROM BENCHMARKS WHERE bid = " + BID2;
            rs = st.executeQuery(bid);
            rs.next();
            benchmark2 = rs.getString("name");


            // Création de la fenêtre principale avec les trois onglets
            Window mainWindow = new Window("Tableau de bord");

            VerticalLayout layout = new VerticalLayout();
            layout.setMargin(true);
            mainWindow.setContent(layout);

            Label texte = new Label();
            texte.setValue("Les données du benchmark à comparer seront comparées avec celles du benchmark de référence.");
            layout.addComponent(texte);
            Label texte2 = new Label();
            texte2.setValue("C'est-à-dire que si un problème est résolu dans un temps t=20s pour le benchmark à comparer et de t=40s pour le benchmark de référence alors on dira qu'il y a une hausse de performance.");
            layout.addComponent(texte2);

            HorizontalLayout layoutCombobox = new HorizontalLayout();
            layoutCombobox.setMargin(true);
            layout.addComponent(layoutCombobox);

            // Insertion des noms des benchmarks dans les combobox
            final ComboBox choixBenchmark1 = new ComboBox(
                    "Benchmark de référence : ");
            for (String nom : nomBenchmarks) {
                choixBenchmark1.addItem(nom);
            }
            choixBenchmark1.setNullSelectionAllowed(false);
            choixBenchmark1.setValue(benchmark1);
            layoutCombobox.addComponent(choixBenchmark1);

            VerticalLayout espace = new VerticalLayout();
            espace.setMargin(true);
            layoutCombobox.addComponent(espace);

            final ComboBox choixBenchmark2 = new ComboBox("Benchmark à comparer : ");
            for (String nom : nomBenchmarks) {
                choixBenchmark2.addItem(nom);
            }
            choixBenchmark2.setNullSelectionAllowed(false);
            choixBenchmark2.setValue(benchmark2);
            layoutCombobox.addComponent(choixBenchmark2);

            // Création des trois onglets : chaque onglet est une nouvelle classe
            // Les classes récupèrent les valeurs des deux benchmarks à comparer
            TabSheet onglets = new TabSheet();
            final VerticalLayout ongletBugs = new VerticalLayout();
            final BugsComponent oB = new BugsComponent(benchmark1, benchmark2, properties);
            ongletBugs.addComponent(oB);
            onglets.addTab(ongletBugs, "Bugs");

            final VerticalLayout ongletPerfs = new VerticalLayout();
            final PerfsComponent oP = new PerfsComponent(benchmark1, benchmark2, properties);
            ongletPerfs.addComponent(oP);
            onglets.addTab(ongletPerfs, "Performances");

            final VerticalLayout ongletRecaps = new VerticalLayout();
            ongletRecaps.addComponent(new RecapComponent(
                    benchmark1, benchmark2, oB.getNombreTotalBugsBenchmark1(), oB
                    .getNombreTotalBugsBenchmark2(), oB.getNombreBugs1(),
                    oB.getNombreBugs2(), oB.getNombreBugs3(),
                    oP.getNbHausseTotal(), oP.getNbPerteTotal()));
            onglets.addTab(ongletRecaps, "Récapitulatif");

            // Listener sur l'onglet pour actualiser en temps réel toute
            // l'application quand on change de benchmark
            choixBenchmark1.addListener(new ValueChangeListener() {
                public void valueChange(ValueChangeEvent event) {
                    setBenchmark1(event.getProperty().getValue().toString());
                    ongletBugs.removeAllComponents();
                    BugsComponent oB1 = new BugsComponent(benchmark1, benchmark2, properties);
                    ongletBugs.addComponent(oB1);
                    ongletPerfs.removeAllComponents();
                    PerfsComponent oP1 = new PerfsComponent(benchmark1, benchmark2, properties);
                    ongletPerfs
                            .addComponent(new PerfsComponent(benchmark1, benchmark2, properties));
                    ongletRecaps.removeAllComponents();
                    ongletRecaps.addComponent(new RecapComponent(benchmark1,
                            benchmark2, oB1.getNombreTotalBugsBenchmark1(), oB1
                            .getNombreTotalBugsBenchmark2(), oB1
                            .getNombreBugs1(), oB1.getNombreBugs2(), oB1
                            .getNombreBugs3(), oP1.getNbHausseTotal(), oP1
                            .getNbPerteTotal()));
                }
            });
            choixBenchmark1.setImmediate(true);

            choixBenchmark2.addListener(new ValueChangeListener() {
                public void valueChange(ValueChangeEvent event) {
                    setBenchmark2(event.getProperty().getValue().toString());
                    ongletBugs.removeAllComponents();
                    BugsComponent oB2 = new BugsComponent(benchmark1, benchmark2, properties);
                    ongletBugs.addComponent(oB2);
                    ongletPerfs.removeAllComponents();
                    PerfsComponent oP2 = new PerfsComponent(benchmark1, benchmark2, properties);
                    ongletPerfs
                            .addComponent(new PerfsComponent(benchmark1, benchmark2, properties));
                    ongletRecaps.removeAllComponents();
                    ongletRecaps.addComponent(new RecapComponent(benchmark1,
                            benchmark2, oB2.getNombreTotalBugsBenchmark1(), oB2
                            .getNombreTotalBugsBenchmark2(), oB2
                            .getNombreBugs1(), oB2.getNombreBugs2(), oB2
                            .getNombreBugs3(), oP2.getNbHausseTotal(), oP2
                            .getNbPerteTotal()));
                }
            });
            choixBenchmark2.setImmediate(true);

            layout.addComponent(onglets);

            mainWindow.addComponent(onglets);
            setMainWindow(mainWindow);
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                cn.close();
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
