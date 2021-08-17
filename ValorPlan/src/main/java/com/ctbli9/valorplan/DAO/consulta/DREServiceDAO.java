package com.ctbli9.valorplan.DAO.consulta;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import com.ctbli9.valorplan.DAO.ClassificacaoCtbServiceDAO;
import com.ctbli9.valorplan.DAO.DepartamentoServiceDAO;
import com.ctbli9.valorplan.modelo.NivelArvore;
import com.ctbli9.valorplan.modelo.ResumoDRE;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

import ctbli9.modelo.ctb.ClasseDRE;
import ctbli9.modelo.ctb.GrupoClasseDRE;
import ctbli9.recursos.LibUtil;

public class DREServiceDAO {
	private Connection con;
	private ResultSet res;
	private Statement stmt;

	private boolean agrupaConta;
	private MesAnoOrcamento mesAno;
	private int numNiveis;
	private StringBuilder filiaisConcatenadas;
	
	private ResumoDRE[] niveisDRE;
	private TreeNode[] eloDepto;
	private List<ClasseDRE> classes;
	private ResumoDRE[] classesDre;
	private ResumoDRE[] gruposDre; 
	private List<GrupoClasseDRE> grupos;
	private TreeNode[] eloClasse;
	
	private int[] monitorCod;
	
	public ResultSet getRes() {
		return res;
	}
	
	public DREServiceDAO(Connection con, boolean agrupaConta, MesAnoOrcamento mesAno, int numNiveis, List<String> filiaisSelecionadas) 
			throws SQLException {
		this.agrupaConta = agrupaConta;
		this.mesAno = mesAno;
		this.numNiveis = numNiveis;
		
		this.filiaisConcatenadas = new StringBuilder();
		filiaisSelecionadas.forEach(f -> this.filiaisConcatenadas.append(f + ","));
		if (this.filiaisConcatenadas.length() > 0)
			this.filiaisConcatenadas.deleteCharAt(this.filiaisConcatenadas.length()-1);
		
		monitorCod = new int[numNiveis];                  // Monitores para cada nivel
				
		this.con = con;
		stmt = con.createStatement();
	}
	
	public void close() {
		try {
			stmt.close();
			res = null;
			stmt = null;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sqlConsulta() throws SQLException {
		NivelArvore nivel = DepartamentoServiceDAO.getNivelArvore(con);
			
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT s.nranomes, ");
		for (int i = 0; i < numNiveis; i++) { // Colunas dos níveis de departamento
			monitorCod[i] = 0;
			sql.append(String.format("vd.niv%02d, vd.dep%02d, ", i+1, i+1));
		}
		sql.append(String.format("d.cdClasse, d.dsClasse, d.cdNivelClasse, "
				+ "cc.cecCdExterno, s.cdCentroCusto, cc.cecDsResumida, "
				+ "ctb.cdConta, ctb.ID_Conta, ctb.dsConta, "
				+ "SUM(s.vlOrcado) AS vlOrcado, SUM(s.vlRealizado) AS vlRealizado "
				+ "FROM TmpSaldos s "
				+ "JOIN EmpCentroCusto cc ON s.cdCentroCusto = cc.cdCentroCusto "
				+ "JOIN EmpCencusDepto cdep ON cc.cdCentroCusto = cdep.cdCentroCusto "
				+ "JOIN CtbConta ctb ON s.ID_Conta = ctb.ID_Conta "
				+ "JOIN (" + DepartamentoServiceDAO.montaScriptArvore(nivel.getMaximo()) + ") vd ON cdep.cdDepartamento = vd.niv%02d "
				+ "JOIN CtbClaConta cct ON cct.nrAno = %d AND cct.ID_Conta = s.ID_Conta "
				+ "AND (cct.cdCentroCusto = 0 or cct.cdCentroCusto = s.cdCentroCusto) "
				+ "JOIN CtbClasseDRE d ON cct.cdClasse = d.cdClasse "
				+ "JOIN TmpCencus ON TmpCencus.cdCentroCusto = cc.cdCentroCusto "
				+ "WHERE (s.vlOrcado <> 0  OR s.vlRealizado <> 0)"
				+ (this.filiaisConcatenadas.length() == 0 ? "" : " AND s.cdFilial IN (" + this.filiaisConcatenadas.toString() + ")")
				+ "AND s.nranomes BETWEEN %04d01 AND %d ",
				nivel.getMaximo(),
				mesAno.getAno(),
				mesAno.getAno(),
				mesAno.getAnoMes()));
		
		sql.append("GROUP BY ");
		for (int i = 0; i < numNiveis; i++) {
			sql.append(String.format("vd.niv%02d, vd.dep%02d, ", i+1, i+1));
		}
		sql.append(" d.cdNivelClasse, d.cdClasse, d.dsClasse, "); 
		
		if(this.agrupaConta) { // POR CONTA
			sql.append(" ctb.cdConta, ctb.ID_Conta, ctb.dsConta, "); 
			sql.append(" cc.cecCdExterno, s.cdCentroCusto, cc.cecDsResumida, "); 
		} else {
			sql.append(" cc.cecCdExterno, s.cdCentroCusto, cc.cecDsResumida, "); 			
			sql.append(" ctb.cdConta, ctb.ID_Conta, ctb.dsConta, "); 
		}
		
		sql.append("s.nranomes;"); 
		
		System.out.println("QUERIE MONTADA: " + sql.toString());
		
		
		res = stmt.executeQuery(sql.toString());
	}

	/*
	 * tipo: 1 = mensal, 2 = anual
	 */
	public TreeNode listarResumo() throws Exception {
		
		int periodo = mesAno.getMes();
		
		long tempoInicio = System.currentTimeMillis();
		
		classes = ClassificacaoCtbServiceDAO.listar(con, mesAno.getAno());
		grupos = new ArrayList<GrupoClasseDRE>();

		// Popula grupos de classes na ordem correta e acha maior nível de classe para criar o array
		int maxNivelClasse = 1;
		GrupoClasseDRE grupoAux = new GrupoClasseDRE();
		for (ClasseDRE classe : classes) {
			if (!classe.getGrupo().equals(grupoAux)) {
				grupoAux = classe.getGrupo();
				grupos.add(new GrupoClasseDRE(grupoAux.getCdGrupoClasse(), grupoAux.getNrAno(), grupoAux.getDsGrupoClasse()));
				if (grupos.size() > 1) 
					grupos.add(new GrupoClasseDRE(grupoAux.getCdGrupoClasse(), grupoAux.getNrAno(), grupoAux.getDsGrupoClasse() + " PERCENTUAL"));
			}
			if (classe.getNivel() > maxNivelClasse)
				maxNivelClasse = classe.getNivel(); 
		}
		
		ResumoDRE resumo = new ResumoDRE(periodo);
		resumo.setDescricao("DRE");
		resumo.setTipo("RAI");
		TreeNode raiz = new DefaultTreeNode(resumo, null);
		raiz.setExpanded(true);

		eloDepto = new TreeNode[numNiveis];
		eloClasse = new TreeNode[maxNivelClasse];
		
		niveisDRE = new ResumoDRE[numNiveis];       // Totalizadores para cada nivel (departamento)
		classesDre = new ResumoDRE[classes.size()];  // Totalizadores para classificações
		gruposDre = new ResumoDRE[grupos.size()];    // Totalizadores para grupos de classificações
		
		/*
		 * MONTA O SQL DA CONSULTA DRE
		 */		
		sqlConsulta();
		
		ResumoDRE dreMes = null;
		MesAnoOrcamento anoRef = new MesAnoOrcamento();
		
		/*
		 * INICIO DO PROCESSAMENTO
		 */
		// TODO duplicar esse código na nova função
		boolean podePassar = false;
		boolean temRegistro = res.next();
		while (temRegistro) {
			
			// 1. Pego a chave
			String chaveMonitor = criaChave(numNiveis, res);
			String chaveAtual = "";
			dreMes = new ResumoDRE(periodo);
			
			// 2. Inicializa mês
			int mes = 1;
			do {
				chaveAtual = criaChave(numNiveis, res);
				anoRef.setMes(mes);
				
				// 3. Verifica se está em registro válido
				if (temRegistro && res.getInt("nrAnoMes") == anoRef.getAnoMes() &&
						chaveMonitor.equals(chaveAtual)) {
					
					dreMes.setValorOrcado(mes, res.getDouble("vlOrcado"));
					dreMes.setValorRealizado(mes, res.getDouble("vlRealizado"));
					podePassar = true;
					
				}
				else {
					dreMes.setValorOrcado(mes, 0);
					dreMes.setValorRealizado(mes, 0);
					podePassar = false;
				}
				
				processarNiveis(raiz, mes, dreMes);
				processarClasses(mes, dreMes);
				
				
				for (int j = 0; j < grupos.size(); j++) {
					if (grupos.get(j).getDsGrupoClasse().endsWith("PERCENTUAL")) {
						if (gruposDre[0].getValorOrcado(mes) > 0)
							gruposDre[j].setValorOrcado(mes, gruposDre[j-1].getValorOrcado(mes) /
									gruposDre[0].getValorOrcado(mes) * 100);
						
						if (gruposDre[0].getValorRealizado(mes) > 0)
							gruposDre[j].setValorRealizado(mes, gruposDre[j-1].getValorRealizado(mes) /
									gruposDre[0].getValorRealizado(mes) * 100);

						if (gruposDre[0].getValorOrcadoAcum() > 0)
							gruposDre[j].setValorOrcadoAcum(gruposDre[j-1].getValorOrcadoAcum() /
									gruposDre[0].getValorOrcadoAcum() * 100);
						
						if (gruposDre[0].getValorRealizadoAcum() > 0)
							gruposDre[j].setValorRealizadoAcum(gruposDre[j-1].getValorRealizadoAcum() /
									gruposDre[0].getValorRealizadoAcum() * 100);
					}
				}
				
				if (podePassar) {
					temRegistro = res.next();
					chaveAtual = criaChave(numNiveis, res);
				}
				++mes;
			} while ((temRegistro && chaveMonitor.equals(chaveAtual)) || mes <= periodo);
			
			
		}// while
		
		long tempoFinal = System.currentTimeMillis();
		double resultado = (tempoFinal - tempoInicio) / 1000.00;
		LibUtil.display("GERAÇÃO FINALIZADA EM "  + " " + resultado + " SEGUNDOS.");
		
		return raiz;
	}

	private void processarClasses(int mes, ResumoDRE dreMes) throws SQLException {
		for (int j = 0; j < classes.size(); j++) {
			ClasseDRE classAux = classes.get(j);
			
			// Achou a classe
			if ( classAux.getCdNivelClasse().length() <= res.getString("cdNivelClasse").length() &&
					classAux.getCdNivelClasse().equals(res.getString("cdNivelClasse")
								.substring(0, classAux.getCdNivelClasse().length())) ) {
			
				/*
				 *  Último nível de classe (mais analítico)
				 *  Processa o penúltimo nível e acha o último nível correto
				 */						
				if (classAux.isTemConta()) {
					dreMes.addValorOrcadoAcum(dreMes.getValorOrcado(mes));
					dreMes.addValorRealizadoAcum(dreMes.getValorRealizado(mes));
					
					montarContas(classesDre[j].getEloLigacao(), dreMes, mes);
				}
				
				// Totaliza a Classe	
				classesDre[j].addValorOrcado(mes, dreMes.getValorOrcado(mes));
				classesDre[j].addValorOrcadoAcum(dreMes.getValorOrcado(mes));
				classesDre[j].addValorRealizado(mes, dreMes.getValorRealizado(mes));
				classesDre[j].addValorRealizadoAcum(dreMes.getValorRealizado(mes));

				// Totaliza o agrupamento
				if (classAux.getNivel() == 1) {
					int igrup = 0;
					int posGrupo = -1;
					for (GrupoClasseDRE grupo : grupos) {
						// Compara o grupo da classe com o grupo do array
						if (posGrupo == -1 && 
								!grupo.getDsGrupoClasse().endsWith("PERCENTUAL") && 
								classAux.getGrupo().equals(grupo)) 
							posGrupo = grupos.indexOf(grupo);
						
						if (posGrupo > -1) {
							gruposDre[posGrupo+igrup].addValorOrcado(mes, dreMes.getValorOrcado(mes));
							gruposDre[posGrupo+igrup].addValorOrcadoAcum(dreMes.getValorOrcado(mes));
							gruposDre[posGrupo+igrup].addValorRealizado(mes, dreMes.getValorRealizado(mes));
							gruposDre[posGrupo+igrup].addValorRealizadoAcum(dreMes.getValorRealizado(mes));
							igrup++;
						}
						
					}
				}							
			}// end Achou a classe
								
		}//fim PROCESSA LISTA DE CLASSES
	}

	private void montarContas(TreeNode eloLigacao, ResumoDRE dreMes, int mes) throws SQLException {
		int periodo = mesAno.getMes();
		
		int idChave = 0;
		if(this.agrupaConta)  // POR CONTA
			idChave = res.getInt("ID_Conta");
		else
			idChave = res.getInt("cdCentroCusto");
		
		TreeNode selec = null;
		ResumoDRE dreContaContabil = null;
		// 1. Achar a conta contábil
		for (TreeNode elo : eloLigacao.getChildren()) {
			
			dreContaContabil = (ResumoDRE) elo.getData();
			// Pega o elo
			if (idChave == dreContaContabil.getChaveClasse()) {
				selec = elo;
				break;
			}			
		}
		
		if (selec == null) {
			dreContaContabil = new ResumoDRE(periodo);
			dreContaContabil.setChaveClasse(idChave);
			if(this.agrupaConta)  // POR CONTA
				dreContaContabil.setDescricao(res.getString("cdConta").trim() + "-" + res.getString("dsConta").trim());
			else
				dreContaContabil.setDescricao(res.getString("cecCdExterno").trim() + "-" + res.getString("cecDsResumida").trim());
			
			selec = new DefaultTreeNode(dreContaContabil, eloLigacao);
		}
		
		dreContaContabil.addValorOrcado(mes, dreMes.getValorOrcado(mes));
		dreContaContabil.addValorOrcadoAcum(dreMes.getValorOrcado(mes));
		dreContaContabil.addValorRealizado(mes, dreMes.getValorRealizado(mes));
		dreContaContabil.addValorRealizadoAcum(dreMes.getValorRealizado(mes));

		
		// 3. Adiciona o nível mais analitico
		if (mes == 1) {
			if(!this.agrupaConta)  // POR CONTA
				dreMes.setDescricao(res.getString("cdConta").trim() + "-" + res.getString("dsConta").trim());
			else
				dreMes.setDescricao(res.getString("cecCdExterno").trim() + "-" + res.getString("cecDsResumida").trim());
			
			new DefaultTreeNode(dreMes, selec);
		}			
	}
	
	
	private void processarNiveis(TreeNode raiz, int mes, ResumoDRE dreMes) throws SQLException {	// Processa todos os niveis
		int periodo = mesAno.getMes();
		
		for (int i = 0; i < numNiveis; i++) {
			
			// Se um nível qualquer mudou
			if (monitorCod[i] != res.getInt(String.format("niv%02d", i+1))) {
				
				monitorCod[i] = res.getInt(String.format("niv%02d", i+1));

				niveisDRE[i] = new ResumoDRE(periodo);
				niveisDRE[i].setTipo("TOT");

				niveisDRE[i].setDescricao(res.getString( String.format("dep%02d", i+1) ));
				if (i == 0)
					eloDepto[i] = new DefaultTreeNode(niveisDRE[i], raiz);
				else
					eloDepto[i] = new DefaultTreeNode(niveisDRE[i], eloDepto[i-1]);
			
				if (i < (numNiveis-1))
					eloDepto[i].setExpanded(true);
				/*
				 *  Está no nível mais analítico (ÚLTIMO NIVEL)
				 */
				if (i == (numNiveis-1)) {
					// Monta DRE
					// ***** REGINALDO INICIO TRECHO COMUM
					GrupoClasseDRE grupoAux = null;
					for (int j = 0; j < classes.size(); j++) { // Processa lista de classes
						ClasseDRE claAux = classes.get(j);
						if (grupoAux == null)
							grupoAux = claAux.getGrupo();
						
						// Mudou de grupo
						if (!claAux.getGrupo().equals(grupoAux)) {
							
							// Acha a posição do grupo
							int igrup = 0;
							for (GrupoClasseDRE grupo : grupos) {
								if (grupoAux.equals(grupo)) {
									gruposDre[igrup] = new ResumoDRE(periodo);
									
									if (grupo.getDsGrupoClasse().endsWith("PERCENTUAL"))
										gruposDre[igrup].setTipo("PER");
									else
										gruposDre[igrup].setTipo("TOT");
									
									gruposDre[igrup].setDescricao(grupo.getDsGrupoClasse());
									new DefaultTreeNode(gruposDre[igrup], eloDepto[(numNiveis-1)]);
								}
								igrup++;
							}
							grupoAux = claAux.getGrupo();
						}//Insere grupo
						
						classesDre[j] = new ResumoDRE(periodo);
						classesDre[j].setDescricao(claAux.getDsClasse());
						
						classesDre[j].setTipo("GRP");  // SUB-TOTAL GRUPO

						if (claAux.getNivel() == 1)
							// Linka elo da classe mais sintética com o último departamento
							eloClasse[claAux.getNivel()-1] = new DefaultTreeNode(classesDre[j], eloDepto[(numNiveis-1)]);
						else
							// Linka elo da sub-classe com a classe no nível anterior 
							eloClasse[claAux.getNivel()-1] = new DefaultTreeNode(classesDre[j], eloClasse[claAux.getNivel()-2]);
						
						if (claAux.isTemConta()) { // Guarda a chave para mergulho
							classesDre[j].setEloLigacao(eloClasse[claAux.getNivel()-1]);
						}
						

						
					}//for Monta DRE
					// FIM Monta DRE
					// ***** REGINALDO FIM TRECHO COMUM
					
					// CARREGA ULTIMO GRUPO
					// Acha a posição do grupo
					int igrup = 0;
					for (GrupoClasseDRE grupo : grupos) {
						if (grupoAux.equals(grupo)) {
							gruposDre[igrup] = new ResumoDRE(periodo);
							
							if (grupo.getDsGrupoClasse().endsWith("PERCENTUAL"))
								gruposDre[igrup].setTipo("PER");
							else
								gruposDre[igrup].setTipo("TOT");
							
							gruposDre[igrup].setDescricao(grupo.getDsGrupoClasse());
							new DefaultTreeNode(gruposDre[igrup], eloDepto[(numNiveis-1)]);
						}
						igrup++;
					}
					
					
				}//if i=nivel maximo
				
			}// Se um nível qualquer mudou
								
			niveisDRE[i].addValorOrcado(mes, dreMes.getValorOrcado(mes));
			niveisDRE[i].addValorOrcadoAcum(dreMes.getValorOrcado(mes));
			niveisDRE[i].addValorRealizado(mes, dreMes.getValorRealizado(mes));
			niveisDRE[i].addValorRealizadoAcum(dreMes.getValorRealizado(mes));
			
		}//laço dos niveis
	}

	private String criaChave(int numNiveis, ResultSet res) throws SQLException {
		String chave = "";
		for (int i = 0; i < numNiveis; i++) {
			chave += res.getInt(String.format("niv%02d", i+1));
		}
		if(this.agrupaConta)  // POR CONTA
			chave += res.getString("cdClasse") + res.getString("cdConta") + res.getString("cdCentroCusto");
		else
			chave += res.getString("cdClasse") + res.getString("cdCentroCusto") + res.getString("cdConta");
		return chave;
	}
}
