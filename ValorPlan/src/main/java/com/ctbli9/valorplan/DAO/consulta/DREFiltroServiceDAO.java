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
import com.ctbli9.valorplan.modelo.ResumoDRE;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

import ctbli9.modelo.Filial;
import ctbli9.modelo.ctb.ClasseDRE;
import ctbli9.modelo.ctb.GrupoClasseDRE;
import ctbli9.recursos.LibUtil;

public class DREFiltroServiceDAO {
	private Connection con;
	private ResultSet res;
	private Statement stmt;

	private int tipo;
	private MesAnoOrcamento mesAno;
	private int[] setoresSelecionados;
	private Filial filial;
	
	public ResultSet getRes() {
		return res;
	}
	
	public DREFiltroServiceDAO(Connection con, int tipo, MesAnoOrcamento mesAno, Filial filial, int[] setoresSelecionados) 
			throws SQLException {
		this.tipo = tipo;
		this.mesAno = mesAno;
		this.filial = filial;
		this.setoresSelecionados = setoresSelecionados;
				
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
		String sql = String.format("SELECT lpad(s.cdCentroCusto, 7, '0') || lpad(d.cdClasse, 7, '0') || ctb.cdConta AS ID, "
				+ "s.nranomes, d.cdClasse, d.dsClasse, d.cdNivelClasse, cc.cecCdExterno, s.cdCentroCusto, cc.cecDsResumida, "
				+ "ctb.cdConta, ctb.dsConta, "
				+ "SUM(s.vlOrcado) AS vlOrcado, SUM(s.vlRealizado) AS vlRealizado "
				+ "FROM TmpSaldos s "
				+ "JOIN EmpCentroCusto cc ON s.cdCentroCusto = cc.cdCentroCusto "
				+ "JOIN CtbConta ctb ON s.ID_Conta = ctb.ID_Conta "
				+ "JOIN CtbClaConta cct ON cct.nrAno = %d AND cct.ID_Conta = s.ID_Conta "
				+                     "AND (cct.cdCentroCusto = 0 or cct.cdCentroCusto = s.cdCentroCusto) "
				+ "JOIN CtbClasseDRE d ON cct.cdClasse = d.cdClasse "
				+ "JOIN TmpCencus ON TmpCencus.cdCentroCusto = cc.cdCentroCusto "
				+ "WHERE (s.vlOrcado <> 0  OR s.vlRealizado <> 0)"
				+ (filial.getCdFilial() > 0 ? " AND s.cdFilial = " + filial.getCdFilial() : "")
				+ "AND s.nranomes BETWEEN %04d01 AND %d "
				+ "GROUP BY ",
				mesAno.getAno(),
				mesAno.getAno(),
				mesAno.getAnoMes());
		
		sql += " cc.cecCdExterno, cc.cecDsResumida, d.cdNivelClasse, d.cdClasse, d.dsClasse, s.cdCentroCusto, ctb.cdConta, ctb.dsConta, s.nranomes;";
		
		System.out.println("QUERIE MONTADA: " + sql);
		
		res = stmt.executeQuery(sql);
	}

	/*
	 * tipo: 1 = mensal, 2 = anual
	 */
	public TreeNode listarResumo() 
			throws Exception {
		int periodo = mesAno.getMes();
		
		long tempoInicio = System.currentTimeMillis();
		
		List<ClasseDRE> classes = ClassificacaoCtbServiceDAO.listar(con, mesAno.getAno());
		List<GrupoClasseDRE> grupos = new ArrayList<GrupoClasseDRE>();

		// Popula grupos de classes na ordem correta e acha maior nível de classe para criar o array
		int numNiveis = setoresSelecionados.length;
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
		resumo.setTipo("RAI");  // Raiz
		TreeNode raiz = new DefaultTreeNode(resumo, null);
		raiz.setExpanded(true);

		TreeNode[] eloDepto = new TreeNode[2];
		TreeNode[] eloClasse = new TreeNode[maxNivelClasse];
		int monitorCod = 0;                  // Monitores para cada nivel
		
		ResumoDRE[] niveisDRE = new ResumoDRE[2];                // Totalizadores para cada nivel (departamento)
		ResumoDRE[] classesDre = new ResumoDRE[classes.size()];  // Totalizadores para classificações
		ResumoDRE[] gruposDre = new ResumoDRE[grupos.size()];    // Totalizadores para grupos de classificações
		
		numNiveis--;  // Deduz 1 para ficar mais fácil a manipulação das tabelas
		
		/*
		 * MONTA O SQL DA CONSULTA DRE
		 */
		sqlConsulta();
		
		ResumoDRE dreMes = null;
		MesAnoOrcamento anoRef = new MesAnoOrcamento();
		
		niveisDRE[0] = new ResumoDRE(periodo);
		niveisDRE[0].setTipo("TOT");


		niveisDRE[0].setDescricao("DRE");
		eloDepto[0] = new DefaultTreeNode(niveisDRE[0], raiz);
		eloDepto[0].setExpanded(true);
		
		/*
		 * INICIO DO PROCESSAMENTO
		 */
		
		boolean podePassar = false;
		boolean temRegistro = res.next();
		while (temRegistro) {
			// 1. Pego a chave
			String chaveMonitor = res.getString("ID");
			dreMes = new ResumoDRE(periodo);
			
			// 2. Inicializa mês
			int mes = 1;
			do {
				anoRef.setMes(mes);
				
				// 3. Verifica se está em registro válido
				if (temRegistro && res.getInt("nrAnoMes") == anoRef.getAnoMes() &&
						chaveMonitor.equals(res.getString("ID"))) {
					
					dreMes.setValorOrcado(mes, res.getDouble("vlOrcado"));
					dreMes.setValorRealizado(mes, res.getDouble("vlRealizado"));
					podePassar = true;
					
				}
				else {
					dreMes.setValorOrcado(mes, 0);
					dreMes.setValorRealizado(mes, 0);
					podePassar = false;
				}
				
				// Se um nível qualquer mudou
				if (monitorCod != res.getInt("cdCentroCusto")) {
					
					monitorCod = res.getInt("cdCentroCusto");

					niveisDRE[1] = new ResumoDRE(periodo);
					niveisDRE[1].setTipo("TOT");

					niveisDRE[1].setDescricao(res.getString("cecCdExterno") + "-" + res.getString("cecDsResumida"));
					eloDepto[1] = new DefaultTreeNode(niveisDRE[1], eloDepto[0]);
				
					/*
					 *  Está no nível mais analítico (ÚLTIMO NIVEL)
					 */
					// Monta DRE
					// ***** REGINALDO INICIO TRECHO COMUM
					grupoAux = null;
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
									new DefaultTreeNode(gruposDre[igrup], eloDepto[1]);
								}
								igrup++;
							}
							grupoAux = claAux.getGrupo();
						}//Insere grupo
						
						classesDre[j] = new ResumoDRE(periodo);
						classesDre[j].setDescricao(claAux.getDsClasse());
						
						if (claAux.isTemConta()) { // Guarda a chave para mergulho
							classesDre[j].setChaveClasse(claAux.getCdClasse());
						} 
						classesDre[j].setTipo("GRP");  // SUB-TOTAL GRUPO

						if (claAux.getNivel() == 1)
							// Linka elo da classe mais sintética com o último departamento
							eloClasse[claAux.getNivel()-1] = new DefaultTreeNode(classesDre[j], eloDepto[1]);
						else
							// Linka elo da sub-classe com a classe no nível anterior 
							eloClasse[claAux.getNivel()-1] = new DefaultTreeNode(classesDre[j], eloClasse[claAux.getNivel()-2]);
						
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
							new DefaultTreeNode(gruposDre[igrup], eloDepto[1]);
						}
						igrup++;
					}
						
						
					
				}// Se um nível qualquer mudou
									
				niveisDRE[0].addValorOrcado(mes, dreMes.getValorOrcado(mes));
				niveisDRE[0].addValorOrcadoAcum(dreMes.getValorOrcado(mes));
				niveisDRE[0].addValorRealizado(mes, dreMes.getValorRealizado(mes));
				niveisDRE[0].addValorRealizadoAcum(dreMes.getValorRealizado(mes));
					
				niveisDRE[1].addValorOrcado(mes, dreMes.getValorOrcado(mes));
				niveisDRE[1].addValorOrcadoAcum(dreMes.getValorOrcado(mes));
				niveisDRE[1].addValorRealizado(mes, dreMes.getValorRealizado(mes));
				niveisDRE[1].addValorRealizadoAcum(dreMes.getValorRealizado(mes));
					

				
				/*
				 *  PROCESSA LISTA DE CLASSES
				 */
				for (int j = 0; j < classes.size(); j++) {
					ClasseDRE classAux = classes.get(j);
					
					// Achou a classe
					if ( classAux.getCdNivelClasse().length() <= res.getString("cdNivelClasse").length() &&
							classAux.getCdNivelClasse().equals(res.getString("cdNivelClasse").substring(0, classAux.getCdNivelClasse().length())) ) {
					
						/*
						 *  Último nível de classe (mais analítico)
						 *  Processa o penúltimo nível e acha o último nível correto
						 */						
						if (classAux.isTemConta()) {
							dreMes.addValorOrcadoAcum(dreMes.getValorOrcado(mes));
							dreMes.addValorRealizadoAcum(dreMes.getValorRealizado(mes));
							// Se estiver no primeiro mês, linka a conta+setor com o nível anterior
							if (mes == 1) {
								if (this.tipo == 1) // Mensal
									dreMes.setDescricao(res.getString("cecCdExterno").trim() + "-" 
											+ res.getString("cecDsResumida").trim() + " => " 
											+ res.getString("cdConta").trim() + "-" + res.getString("dsConta").trim());
								else
									dreMes.setDescricao(res.getString("cecDsResumida").trim() + " => " + res.getString("cdConta").trim());
								
								TreeNode elo = achaElo(eloDepto[1], null, res.getInt("cdClasse"));
								new DefaultTreeNode(dreMes, elo);
							}
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
								if (posGrupo == -1 && !grupo.getDsGrupoClasse().endsWith("PERCENTUAL") && classAux.getGrupo().equals(grupo)) 
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
				
				// Incluir último grupo
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
				}
				++mes;
			} while ((temRegistro && chaveMonitor.equals(res.getString("ID"))) || mes <= periodo);
			
		}// while
		
		long tempoFinal = System.currentTimeMillis();
		double resultado = (tempoFinal - tempoInicio) / 1000.00;
		LibUtil.display("GERAÇÃO FINALIZADA EM "  + " " + resultado + " SEGUNDOS.");
		
		return raiz;
	}


	private TreeNode achaElo(TreeNode treeNode, TreeNode selec, int codClasse) {
		for (TreeNode elo : treeNode.getChildren()) {
			if (elo.getChildCount() > 0) {
				selec = achaElo(elo, selec, codClasse);
				if (selec != null)
					return selec;
			}
			
			ResumoDRE resAux = (ResumoDRE) elo.getData();
			if (resAux.getChaveClasse() == codClasse) {
				return elo;
			}
		}
		
		return null;
	}

}
