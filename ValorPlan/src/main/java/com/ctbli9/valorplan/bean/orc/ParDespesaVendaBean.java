package com.ctbli9.valorplan.bean.orc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.ctbli9.valorplan.DAO.PlanoServiceDAO;
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.DespesaVenda;
import com.ctbli9.valorplan.modelo.FiltroReceita;
import com.ctbli9.valorplan.modelo.orc.Recurso;
import com.ctbli9.valorplan.modelo.receita.Receita;
import com.ctbli9.valorplan.negocio.EquipeService;
import com.ctbli9.valorplan.negocio.ReceitaService;
import com.ctbli9.valorplan.negocio.orc.ParamDespesaService;
import com.ctbli9.valorplan.recursos.Global;

import ctbli9.enumeradores.StatusPlano;
import ctbli9.recursos.DataUtil;
import ctbli9.recursos.FacesMessages;
import com.ctbli9.valorplan.recursos.LibUtilFaces;

import com.ctbli9.valorplan.recursos.MesAnoOrcamento;
import ctbli9.recursos.RegraNegocioException;


@ManagedBean(name="despesaVendaBean")
@ViewScoped
public class ParDespesaVendaBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<Receita> receitas = new ArrayList<Receita>();
	private List<Receita> receitasFiltradas = new ArrayList<Receita>();

	private Receita receita;
	private Receita receitaOrigem;
	private List<DespesaVenda> listaParDespesas = new ArrayList<DespesaVenda>();
	private DespesaVenda despesaVenda;
	private List<Recurso> funcionarios;
	private Recurso funcionario;
	private boolean opcaoVisualizacao;
	
	private String escopoReplicacao;
	
	private StreamedContent file;
	private String nomeXLS;
	
	private FacesMessages msg;
	private MesAnoOrcamento mesRef = new MesAnoOrcamento();
	
	/*
	 * Atributos
	 */
	public List<Receita> getReceitas() {
		return receitas;
	}
	
	public List<Receita> getReceitasFiltradas() {
		return receitasFiltradas;
	}
	public void setReceitasFiltradas(List<Receita> receitasFiltradas) {
		this.receitasFiltradas = receitasFiltradas;
	}
	
	public Receita getReceita() {
		return receita;
	}
	public void setReceita(Receita receita) {
		this.receita = receita;
	}
	
	public Receita getReceitaOrigem() {
		return receitaOrigem;
	}
	public void setReceitaOrigem(Receita receitaOrigem) {
		this.receitaOrigem = receitaOrigem;
	}
	
	public List<DespesaVenda> getListaParDespesas() {
		return listaParDespesas;
	}
	public void setListaParDespesas(List<DespesaVenda> listaParDespesas) {
		this.listaParDespesas = listaParDespesas;
	}

	public Recurso getFuncionario() {
		return funcionario;
	}
	public void setFuncionario(Recurso funcionario) {
		this.funcionario = funcionario;
	}
	
	public boolean isOpcaoVisualizacao() {
		return opcaoVisualizacao;
	}
	public void setOpcaoVisualizacao(boolean opcaoVisualizacao) {
		this.opcaoVisualizacao = opcaoVisualizacao;
	}
	
	public List<Recurso> getFuncionarios() {
		return funcionarios;
	}
		
	public DespesaVenda getDespesaVenda() {
		return despesaVenda;
	}
	public void setDespesaVenda(DespesaVenda despesaVenda) {
		this.despesaVenda = despesaVenda;
	}
	
	public String getEscopoReplicacao() {
		return escopoReplicacao;
	}
	
	/*
	 * Metodos
	 */
	public void inicializar() {
		this.msg = new FacesMessages();
		
		this.funcionario = new Recurso(new MesAnoOrcamento());
		this.setListaParDespesas(new ArrayList<DespesaVenda>());
		
		this.receitaOrigem = new Receita();
		
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			FiltroReceita filtro = new FiltroReceita();
			filtro.setContaAtiva(true);
			filtro.setContaInativa(false);
			filtro.setCdCategorias(new ReceitaService(con).listarTiposReceita());
			this.receitas = new ReceitaService(con).listarReceitas(filtro);
			this.receitasFiltradas = this.receitas;
			
			this.funcionarios = new EquipeService(con.getConexao()).listarRecursosPorGestor(
					Global.getFuncionarioLogado().getCdFuncionario(), new MesAnoOrcamento(), true);
			
			ConexaoDB.gravarTransacao(con);
		} catch (Exception e) {
			ConexaoDB.desfazerTransacao(con);
			e.printStackTrace();
			msg.erro("ERRO ao listar receitas: " + e.getMessage());
			
		} finally {
			ConexaoDB.close(con);			
		} 
		
	}//end inicializar
	
	public void listarParamDespesas() {
		if(this.funcionario != null && this.funcionario.getCdRecurso() > 0) {
			ConexaoDB con = null;
			try {
				con = new ConexaoDB();
				
				if (this.receita == null)
					throw new RegraNegocioException("Selecione uma Receita.");
				
				this.setListaParDespesas(new ParamDespesaService(con).listarDespesaVenda(this.receita, this.funcionario));
				
			} catch (RegraNegocioException e) {
				this.msg.erro(e.getMessage());
				
			} catch (Exception e) {
				this.msg.erro("ERRO ao listar despesas: " + e.getMessage());
				e.printStackTrace();
				
			} finally {
				ConexaoDB.close(con);		
			} 
 
		} else {
			this.setListaParDespesas(new ArrayList<DespesaVenda>());
		}
	}
	
	public void limparReceitaOrcamento() {
		this.funcionario = new Recurso(new MesAnoOrcamento());
		this.setListaParDespesas(new ArrayList<DespesaVenda>());		
	}
	
	public boolean isParametroSelecionado() {
		return this.funcionario != null && this.funcionario.getCdRecurso() > 0 &&
				PlanoServiceDAO.getPlanoSelecionado().getStatus().equals(StatusPlano._0);
	}
	
	public void salvar(ActionEvent event) {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			new ParamDespesaService(con).salvarDespesaVenda(this.receita, this.funcionario,
					this.listaParDespesas);
			
			msg.info("Despesas gravadas com sucesso!");
			ConexaoDB.gravarTransacao(con);			
			
		}  catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);			
		}
	}//end salvar

	public void replicar(DespesaVenda item, int pos) {
		if (item.getPercDespesa()[pos] != null && item.getPercDespesa()[pos].compareTo(BigDecimal.ZERO) > 0) {
			for (int i = (pos+1); i < item.getPercDespesa().length; i++) {
				if (item.getPercDespesa()[i] == null || item.getPercDespesa()[i].compareTo(BigDecimal.ZERO) == 0) {
					item.getPercDespesa()[i] = item.getPercDespesa()[pos];
				}
			}
		}
	}

	public void prepararReplicacao(String escopoReplicacao) {
		this.escopoReplicacao = escopoReplicacao;
	}
	
	
	public void replicarDadosVendedor() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			if (this.escopoReplicacao.equals("VEND")) {
				PlanoServiceDAO.log("pdv", "REPLICAR VENDEDOR - INICIO");
				new ParamDespesaService(con).duplicarDadosCusvoVendaVendedor(this.funcionario.getSetor(), this.funcionarios, 
						this.receita, this.funcionario, this.listaParDespesas);
				PlanoServiceDAO.log("pdv", "REPLICAR VENDEDOR - FIM");
			} else {
				PlanoServiceDAO.log("pdv", "REPLICAR CENTRO DE CUSTO - INICIO");
				new ParamDespesaService(con).duplicarDadosCusvoVendaVendedor(null, this.funcionarios, 
						this.receita, this.funcionario, this.listaParDespesas);
				PlanoServiceDAO.log("pdv", "REPLICAR CENTRO DE CUSTO - FIM");
			}
			
			ConexaoDB.gravarTransacao(con);		
			
			this.msg.info("Parâmetros replicados com sucesso.");
			LibUtilFaces.atualizarView("frm:messages");
			
			
		}  catch (Exception e) {
			Global.erro(con, e, msg, null);
			LibUtilFaces.atualizarView("frm:messages");
		} finally {
			ConexaoDB.close(con);			
		}
	}//duplicarDados

	// TODO habilitar novamente essa função
	/*public void replicarDespesa() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			new ParamDespesaService(con).replicarGeralDespesa(this.funcionarios, this.receita, this.despesaVenda);				
			
			this.msg.info("Parâmetros replicados com sucesso.");
			LibUtilFaces.atualizarView("frm:messages");
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
			LibUtilFaces.atualizarView("frm:messages");
		} finally {
			ConexaoDB.close(con);			
		}	
		
	}//replicarDespesa
	*/
	
	
	public void baixarPlanilha() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			String caminho = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
			this.nomeXLS = "ParamDeducoes_" + DataUtil.dataString(new Date(), "yyyy_MM_dd_HH_mm_ss") + ".xls";
			caminho = caminho + "relatorios/" + this.nomeXLS;
			
			new ParamDespesaService(con).gerarPlanilha(caminho);
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
			
		} finally {
			ConexaoDB.close(con);
		}
	}
	

	public StreamedContent getFile() throws FileNotFoundException {
		String caminhoWebInf = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
		caminhoWebInf = caminhoWebInf + "relatorios/" + this.nomeXLS;
		InputStream stream = new FileInputStream(caminhoWebInf); //Caminho onde esta salvo o arquivo.
        
		file = DefaultStreamedContent.builder()
                .name(this.nomeXLS)
                .contentType("application/xls")
                .stream(() -> stream)
                .build();   

        return file;  
    }

	
	public boolean mesFechado(int mes) {
		mesRef.setMes(mes);
		return !mesRef.isMesAberto();
	}


}
