package choco.ui;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class BugsComponent extends CustomComponent {
	// Récupération des noms des problèmes pour les BENCHMARKS 1 et 2 et les
	// 2 benchmarks
	final int[] benchs = new int[]{0,1};
	// 3 bug kinds
	final int[] bugs = new int[]{0,1,2};
	public static final int NO_SOL = 0, WRONG_OPT = 2, SUSPICIOUS_VALUE = 1;
	// data
	int[] nbBugs = new int[3];
	List<String>[][] prob_name = new List[2][3];
	int[] nombreTotalBugsBenchmark = new int[2];

	//TODO bug solution found while unsat problem

	public BugsComponent(String benchmark1, String benchmark2, Properties mysql) {
		// SQL Queries
		loadData(benchmark1,benchmark2,mysql);
		// GUI
		final VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setMargin(true);

		final Table table = new Table("Bugs");
		table.setSortDisabled(true);
		table.setSizeFull();
		table.setHeight("600px");
		table.addContainerProperty("Bug kind", String.class, null);
		table.addContainerProperty("Benchmark name", String.class, null);
		table.addContainerProperty("Problem name", String.class, null);

		displayBugTable(table, NO_SOL, benchmark1, benchmark2, "SAT : time < limit and no solution while exists (or opposite)");
		displayBugTable(table, SUSPICIOUS_VALUE, benchmark1, benchmark2, "Suspicious objective value");
		displayBugTable(table, WRONG_OPT, benchmark1, benchmark2, "OPT : time < limit and bad solution (wrong optimum)");

		layout.addComponent(table);
		setCompositionRoot(layout);
	}


	public void loadData(String benchmark1, String benchmark2, Properties mysql) {
		// Récupération des identifiants pour se connecter à la BDD
		Connection cn = null;
		Statement st = null;
		for (int b : benchs) {
			for (int u : bugs) {
				prob_name[b][u] = new ArrayList();
			}
		}
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
			String requetebid1 = "SELECT bid FROM BENCHMARKS WHERE name = '" + benchmark1 + "'";
			ResultSet rs = st.executeQuery(requetebid1);
			rs.next();
			Integer BID1 = rs.getInt("bid");

			String requetebid2 = "SELECT bid FROM BENCHMARKS WHERE name = '" + benchmark2 + "'";
			rs = st.executeQuery(requetebid2);
			rs.next();
			Integer BID2 = rs.getInt("bid");

			loadUnsat(st, BID1, BID2);
			loadSuspicious(st, benchmark1, benchmark2);
			loadWrongOpt(st, BID1, BID2);

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

	private void loadUnsat(Statement st, Integer BID1, Integer BID2) throws SQLException {
		// Solving_time < 900000 and no solution
		// benchmark 1
		String query = "Select p.name FROM PROBLEMS p, RESOLUTIONS r WHERE bid = "+ BID1+ " AND r.pid=p.pid AND p.resolution='SAT' " +
				"AND r.solving_time<900000 AND r.nb_sol=0 AND p.objective<>0 AND p.optimal=1";
		ResultSet rs = st.executeQuery(query);
		while (rs.next()) {
			prob_name[0][NO_SOL].add(rs.getString("name"));
		}
		query = "Select p.name FROM PROBLEMS p, RESOLUTIONS r WHERE bid = "+ BID1+ " AND r.pid=p.pid AND p.resolution='SAT' " +
						"AND r.solving_time<900000 AND r.nb_sol>0 AND p.objective=0 AND p.optimal=1";
		rs = st.executeQuery(query);
		while (rs.next()) {
			prob_name[0][NO_SOL].add(rs.getString("name"));
		}
		// lbenchmark 2
		query = "Select p.name FROM PROBLEMS p, RESOLUTIONS r WHERE bid = " + BID2 + " AND r.pid=p.pid AND p.resolution='SAT' " +
						"AND r.solving_time<900000 AND r.nb_sol=0 AND p.objective<>0 AND p.optimal=1";
		rs = st.executeQuery(query);
		while (rs.next()) {
			prob_name[1][NO_SOL].add(rs.getString("name"));
		}
		query = "Select p.name FROM PROBLEMS p, RESOLUTIONS r WHERE bid = " + BID2 + " AND r.pid=p.pid AND p.resolution='SAT' " +
								"AND r.solving_time<900000 AND r.nb_sol>0 AND p.objective=0 AND p.optimal=1";
		rs = st.executeQuery(query);
		while (rs.next()) {
			prob_name[1][NO_SOL].add(rs.getString("name"));
		}
	}

	private void loadWrongOpt(Statement st, Integer BID1, Integer BID2) throws SQLException {
		// TODO distinguer mieux que opt (donc faux) et moins bien que meilleure solution connues (donc faux, mais on connait pas l'opt)
		// benchmark 1
		String query = "SELECT p.name FROM PROBLEMS p, RESOLUTIONS r WHERE bid = "+ BID1+ " AND r.solving_time<900000 AND r.pid=p.pid AND nb_sol<> 0 AND p.objective<>r.objective ";
		ResultSet rs = st.executeQuery(query);
		while (rs.next()) {
			prob_name[0][WRONG_OPT].add(rs.getString("name"));
		}
		// benchmark 2
		query = "SELECT p.name FROM PROBLEMS p, RESOLUTIONS r WHERE bid = "+ BID2+ " AND r.solving_time<900000 AND r.pid=p.pid AND nb_sol<> 0 AND p.objective<>r.objective";
		rs = st.executeQuery(query);
		while (rs.next()) {
			prob_name[1][WRONG_OPT].add(rs.getString("name"));
		}
	}

	private void loadSuspicious(Statement st, String benchmark1, String benchmark2) throws SQLException {
		// Requête valeur exceptionnelle
		Integer BID = 0;
		List<Integer> pids = new ArrayList<Integer>();
		// On récupère tous les IDs des problèmes
		String reqNbPbs = "SELECT pid FROM PROBLEMS";
		ResultSet rs = st.executeQuery(reqNbPbs);
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
				// On le compare avec la moyenne pour savoir si ça varie de 30% ou non
				if (moyenne * 0.3 >= valeur) {
					String nomProbleme = rs.getString("name");
					String requeteBID = "SELECT * FROM BENCHMARKS where bid ="
							+ BID;
					rs = st.executeQuery(requeteBID);
					rs.next();
					String nomBenchmark = rs.getString("name");
					if (benchmark1.equals(nomBenchmark)) {
						prob_name[0][SUSPICIOUS_VALUE].add(nomProbleme);
					} else {
						if (benchmark2.equals(nomBenchmark)) {
							prob_name[1][SUSPICIOUS_VALUE].add(nomProbleme);
						}
					}
				}
			} else {
				// On le compare avec la moyenne pour savoir si ça varie de 30% ou non
				if (objective * 0.3 >= valeur) {
					String nomProbleme = rs.getString("name");
					String requeteBID = "SELECT * FROM BENCHMARKS where bid ="
							+ BID;
					rs = st.executeQuery(requeteBID);
					rs.next();
					String nomBenchmark = rs.getString("name");
					if (benchmark1.equals(nomBenchmark)) {
						prob_name[0][SUSPICIOUS_VALUE].add(nomProbleme);
					} else {
						if (benchmark2.equals(nomBenchmark)) {
							prob_name[1][SUSPICIOUS_VALUE].add(nomProbleme);
						}
					}
				}
			}
		}
	}

	public void displayBugTable(Table table, int bug, String benchmark1, String benchmark2, String label){
		int basis = 1000*bug;
		table.addItem(new Object[]{label, "", ""}, new Integer(basis));
		if (prob_name[0][bug].equals(null) || prob_name[0][bug].size() == 0
				&& prob_name[1][bug].equals(null) || prob_name[1][bug].size() == 0) {
			table.addItem(new Object[]{"", "No result", "No result"},new Integer(basis+1));
		} else {
			for (int i = basis+1; i < basis+1 + prob_name[0][bug].size(); i++) {
				table.addItem(new Object[]{"", benchmark1,prob_name[0][bug].get(i - (basis+1))}, new Integer(i));
			}
			for (int i = basis+500; i < basis+500 + prob_name[1][bug].size(); i++) {
				table.addItem(new Object[]{"", benchmark2,prob_name[1][bug].get(i - (basis+500))}, new Integer(i));
			}
			nombreTotalBugsBenchmark[0] += prob_name[0][bug].size();
			nombreTotalBugsBenchmark[1] += prob_name[1][bug].size();
			nbBugs[bug] = prob_name[0][bug].size() + prob_name[1][bug].size();
		}
	}

	public int getNombreTotalBugsBenchmark(int bench) {
		return nombreTotalBugsBenchmark[bench];
	}

	public Integer getNombreBugs(int kind) {
		return nbBugs[kind];
	}
}
