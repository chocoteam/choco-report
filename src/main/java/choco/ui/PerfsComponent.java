package choco.ui;

import com.invient.vaadin.charts.Color.RGB;
import com.invient.vaadin.charts.InvientCharts;
import com.invient.vaadin.charts.InvientCharts.DecimalPoint;
import com.invient.vaadin.charts.InvientCharts.SeriesType;
import com.invient.vaadin.charts.InvientCharts.XYSeries;
import com.invient.vaadin.charts.InvientChartsConfig;
import com.invient.vaadin.charts.InvientChartsConfig.AxisBase.AxisTitle;
import com.invient.vaadin.charts.InvientChartsConfig.*;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Properties;

@SuppressWarnings("serial")
public class PerfsComponent extends CustomComponent {

    // Récupération des identifiants pour se connecter à la BDD
    Connection cn = null;
    Statement st = null;
    ResultSet rs = null;
    // Hashmap pour les couleurs du graphique
    @SuppressWarnings("unused")
    private final HashMap<String, RGB> colors = new HashMap<String, RGB>();
    VerticalLayout layout = new VerticalLayout();
    HorizontalLayout layoutMin = new HorizontalLayout();
    HorizontalLayout layoutMax = new HorizontalLayout();
    HorizontalLayout layoutSat = new HorizontalLayout();

    int bid1 = 0;
    int bid2 = 0;

    Integer nbHausseTotal = 0;
    Integer nbPerteTotal = 0;

    public Integer getNbHausseTotal() {
        return nbHausseTotal;
    }

    public void setNbHausseTotal(Integer nbHausseTotal) {
        this.nbHausseTotal = nbHausseTotal;
    }

    public Integer getNbPerteTotal() {
        return nbPerteTotal;
    }

    public void setNbPerteTotal(Integer nbPerteTotal) {
        this.nbPerteTotal = nbPerteTotal;
    }

    public PerfsComponent(String benchmark1, String benchmark2, Properties mysql) {

        HashMap<Integer, Double> bencmark1 = new HashMap<Integer, Double>();
        HashMap<Integer, Double> bencmark2 = new HashMap<Integer, Double>();

        layout.setMargin(true);

        try {
            String url = mysql.getProperty("mysql.url");
            String dbname = mysql.getProperty("mysql.dbname");
            String user = mysql.getProperty("mysql.user");
            String pwd = mysql.getProperty("mysql.pwd");
            // Connexion à la BDD
            Class.forName("com.mysql.jdbc.Driver");

            cn = DriverManager.getConnection("jdbc:mysql://" + url + "/" + dbname, user, pwd);
            st = cn.createStatement();

            // R�cup�ration du benchmark avec le BID 1
            String reqBid1 = "Select bid FROM BENCHMARKS b WHERE b.name ='"
                    + benchmark1 + "'";
            rs = st.executeQuery(reqBid1);
            rs.next();
            bid1 = rs.getInt("bid");

            // R�cup�ration du benchmark avec le BID 2
            String reqBid2 = "Select bid FROM BENCHMARKS b WHERE b.name ='"
                    + benchmark2 + "'";
            rs = st.executeQuery(reqBid2);
            rs.next();
            bid2 = rs.getInt("bid");

            /* MIN SOLVING TIME */

            // Requ�te pour r�cup�rer solving_time du benchmark1
            String reqSolvingTime1 = "Select solving_time, r.pid FROM RESOLUTIONS r, PROBLEMS p WHERE p.resolution ='"
                    + "MIN" + "'AND r.bid ='" + bid1 + "' AND r.pid = p.pid";
            bencmark1 = executeSQL(reqSolvingTime1, st, "PID", "Solving_Time");

            // Requ�te pour r�cup�rer solving_time du benchmark2
            String reqSolvingTime2 = "Select solving_time, r.pid FROM RESOLUTIONS r, PROBLEMS p WHERE p.resolution ='"
                    + "MIN" + "' AND r.pid = p.pid AND r.bid ='" + bid2 + "'";
            bencmark2 = executeSQL(reqSolvingTime2, st, "PID", "Solving_Time");

            InvientCharts chart1 = this.createChart("MIN",
                    "Différence de temps de résolution par PID", "PID",
                    "Solving_Time", bencmark1, bencmark2);

            /* MAX SOLVING TIME */

            String lastBenchmark = "SELECT solving_time, r.pid FROM RESOLUTIONS r, PROBLEMS p WHERE p.resolution ='"
                    + "MAX" + "' AND r.pid = p.pid AND r.bid ='" + bid1 + "'";

            bencmark1 = executeSQL(lastBenchmark, st, "PID", "Solving_Time");

            String olderBenchmark = "SELECT solving_time, r.pid FROM RESOLUTIONS r, PROBLEMS p WHERE p.resolution ='"
                    + "MAX" + "' AND r.pid = p.pid AND r.bid ='" + bid2 + "'";

            bencmark2 = executeSQL(olderBenchmark, st, "PID", "Solving_Time");

            InvientCharts chart2 = this.createChart("MAX",
                    "Différence de temps de résolution par PID", "PID",
                    "Solving_Time", bencmark1, bencmark2);

            /* MIN VALUE */

            String rqstMinValue1 = "SELECT r.objective, r.pid FROM RESOLUTIONS r, PROBLEMS p WHERE p.resolution ='"
                    + "MIN" + "' AND r.pid = p.pid AND r.bid ='" + bid1 + "'";
            String rqstMinValue2 = "SELECT r.objective, r.pid FROM RESOLUTIONS r, PROBLEMS p WHERE p.resolution ='"
                    + "MIN" + "' AND r.pid = p.pid AND r.bid ='" + bid2 + "'";

            bencmark1 = executeSQL(rqstMinValue1, st, "PID", "objective");
            bencmark2 = executeSQL(rqstMinValue2, st, "PID", "objective");

            InvientCharts chart3 = this.createChart("MIN",
                    "Différence de valeur par PID", "PID", "Value", bencmark1,
                    bencmark2);

            /* MAX VALUE */

            String rqstMaxValue1 = "SELECT r.objective, r.pid FROM RESOLUTIONS r, PROBLEMS p WHERE p.resolution ='"
                    + "MAX" + "' AND r.pid = p.pid AND r.bid ='" + bid1 + "'";
            String rqstMaxValue2 = "SELECT r.objective, r.pid FROM RESOLUTIONS r, PROBLEMS p WHERE p.resolution ='"
                    + "MAX" + "' AND r.pid = p.pid AND r.bid ='" + bid2 + "'";

            bencmark1 = executeSQL(rqstMaxValue1, st, "PID", "objective");
            bencmark2 = executeSQL(rqstMaxValue2, st, "PID", "objective");

            InvientCharts chart4 = this.createChart("MAX",
                    "Différence de valeur par PID", "PID", "Value", bencmark1,
                    bencmark2);

            /* SAT SOLVING_TIME */

            String reqSolvingTimeSAT1 = "Select solving_time, r.pid FROM RESOLUTIONS r, PROBLEMS p WHERE p.resolution ='"
                    + "SAT" + "'AND r.bid ='" + bid1 + "' AND r.pid = p.pid";
            String reqSolvingTimeSAT2 = "Select solving_time, r.pid FROM RESOLUTIONS r, PROBLEMS p WHERE p.resolution ='"
                    + "SAT" + "'AND r.bid ='" + bid2 + "' AND r.pid = p.pid";

            bencmark1 = executeSQL(reqSolvingTimeSAT1, st, "PID",
                    "Solving_Time");
            bencmark2 = executeSQL(reqSolvingTimeSAT2, st, "PID",
                    "Solving_Time");

            InvientCharts chart5 = this.createChart("SAT",
                    "Différence de temps de résolution par PID", "PID",
                    "Solving_Time", bencmark1, bencmark2);

            /* SAT SOLUTION */
            String reqSolutionSAT1 = "Select nb_sol, r.pid FROM RESOLUTIONS r, PROBLEMS p WHERE p.resolution ='"
                    + "SAT" + "'AND r.bid ='" + bid1 + "' AND r.pid = p.pid";
            String reqSolutionSAT2 = "Select nb_sol, r.pid FROM RESOLUTIONS r, PROBLEMS p WHERE p.resolution ='"
                    + "SAT" + "'AND r.bid ='" + bid2 + "' AND r.pid = p.pid";

            bencmark1 = executeSQL(reqSolutionSAT1, st, "PID", "Nb_Sol");
            bencmark2 = executeSQL(reqSolutionSAT2, st, "PID", "Nb_Sol");

            InvientCharts chart6 = this.createChart("SAT",
                    "Différence de nombre de solutions par PID", "PID",
                    "Nb_Sol", bencmark1, bencmark2);

            // On ajoute les graphes au layout
            layoutMin.addComponent(chart1);
            layoutMax.addComponent(chart2);
            layoutMin.addComponent(chart3);
            layoutMax.addComponent(chart4);
            layoutSat.addComponent(chart5);
            layoutSat.addComponent(chart6);

            layout.addComponent(layoutMin);
            layout.addComponent(layoutMax);
            layout.addComponent(layoutSat);

            setCompositionRoot(layout);

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
    }

    // Méthode pour exécuter une requête SQL
    public HashMap<Integer, Double> executeSQL(String requete, Statement st,
                                               String xAxis, String yAxis) {
        ResultSet rs = null;
        HashMap<Integer, Double> bm1 = new HashMap<Integer, Double>();
        try {
            rs = st.executeQuery(requete);

            while (rs.next()) {
                bm1.put(rs.getInt(xAxis), rs.getDouble(yAxis));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bm1;
    }

    // Méthode pour créer un graphe
    public InvientCharts createChart(String type, String title,
                                     String xAxisName, String yAxisName,
                                     HashMap<Integer, Double> bencmark1,
                                     HashMap<Integer, Double> bencmark2) {

        LinkedHashSet<DecimalPoint> data = new LinkedHashSet<DecimalPoint>();
        InvientChartsConfig chartConfig = new InvientChartsConfig();
        chartConfig.getGeneralChartConfig().setType(SeriesType.COLUMN);
        SubTitle subT = new SubTitle();
        subT.setText(title);
        chartConfig.setSubtitle(subT);
        chartConfig.getTitle().setText(type);
        ColumnConfig columnConf = new ColumnConfig();
        columnConf.setShowInLegend(true);
        columnConf.setPointWidth(5);
        chartConfig.addSeriesConfig(columnConf);
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setTitle(new AxisTitle(xAxisName));
        chartConfig.addXAxes(xAxis);
        NumberYAxis yAxis = new NumberYAxis();
        yAxis.setTitle(new AxisTitle(yAxisName));
        chartConfig.addYAxes(yAxis);
        chartConfig.getTooltip().setFormatterJsFunc(
                "function() {" + " return 'PID : '+ this.x + ' ; " + yAxisName
                        + " : ' + this.y; " + "}");
        InvientCharts chart = new InvientCharts(chartConfig);
        XYSeries series = new XYSeries("Données");

        PointConfig pointConf = new PointConfig(false);

        if (type == "SAT" && yAxisName == "Nb_Sol") {
            data = this.createDataSat(bencmark1, bencmark2, series);
        } else if (yAxisName == "Value") {
            data = this.createDataValue(bencmark1, type, bencmark2, series);
        } else {
            for (Integer key : bencmark1.keySet()) {
                if (!(bencmark1.get(key).equals(bencmark2.get(key)))) {
                    if (bencmark1.get(key) != null
                            && bencmark2.get(key) != null) {
                        if (bencmark1.get(key) < bencmark2.get(key)) {
                            pointConf = new PointConfig(new RGB(51, 153, 51));
                            nbHausseTotal++;
                        } else {
                            pointConf = new PointConfig(new RGB(255, 0, 51));
                            nbPerteTotal++;
                        }
                        double tmp = bencmark1.get(key) - bencmark2.get(key);
                        if (tmp < 0) {
                            tmp = tmp * -1;
                        }
                        data.add(new DecimalPoint(series, key, tmp, pointConf));
                    }
                }
            }
        }

        series.setSeriesPoints(data);
        chart.addSeries(series);
        chart.setStyleName("v-chart-min-width");
        chart.setSizeFull();
        return chart;
    }

    // Méthode pour récupérer des données pour le graphe
    public LinkedHashSet<DecimalPoint> createDataSat(
            HashMap<Integer, Double> bencmark1,
            HashMap<Integer, Double> bencmark2, XYSeries series) {

        PointConfig pointConf = new PointConfig(false);
        LinkedHashSet<DecimalPoint> data = new LinkedHashSet<DecimalPoint>();
        for (Integer key : bencmark1.keySet()) {
            if (!(bencmark1.get(key).equals(bencmark2.get(key)))) {
                double tmp = bencmark1.get(key) - bencmark2.get(key);

                if (bencmark1.get(key) >= 1 && bencmark2.get(key) == 0) {
                    pointConf = new PointConfig(new RGB(51, 153, 51)); // vert
                    tmp = 1;
                    nbHausseTotal++;
                } else if (bencmark1.get(key) == 0 && bencmark2.get(key) >= 1) {
                    pointConf = new PointConfig(new RGB(255, 0, 51)); // rouge
                    tmp = -1;
                    nbPerteTotal++;
                }
                data.add(new DecimalPoint(series, key, tmp, pointConf));
            }
        }

        return data;
    }

    // Méthode pour récupérer des données pour le graphe
    public LinkedHashSet<DecimalPoint> createDataValue(
            HashMap<Integer, Double> bencmark1, String type,
            HashMap<Integer, Double> bencmark2, XYSeries series) {

        PointConfig pointConf = new PointConfig(false);
        LinkedHashSet<DecimalPoint> data = new LinkedHashSet<DecimalPoint>();
        for (Integer key : bencmark1.keySet()) {
            if (!(bencmark1.get(key).equals(bencmark2.get(key)))) {
                double tmp = bencmark1.get(key) - bencmark2.get(key);
                if (type == "MAX") {
                    if (tmp < 0) {
                        pointConf = new PointConfig(new RGB(255, 0, 51));
                        tmp = tmp * -1;
                        nbPerteTotal++;
                    } else {
                        pointConf = new PointConfig(new RGB(51, 153, 51));
                        nbHausseTotal++;
                    }
                } else {
                    if (tmp > 0) {
                        pointConf = new PointConfig(new RGB(255, 0, 51));
                        nbPerteTotal++;
                    } else {
                        pointConf = new PointConfig(new RGB(51, 153, 51));
                        tmp = tmp * -1;
                        nbHausseTotal++;
                    }
                }
                data.add(new DecimalPoint(series, key, tmp, pointConf));
            }
        }

        return data;
    }
}
