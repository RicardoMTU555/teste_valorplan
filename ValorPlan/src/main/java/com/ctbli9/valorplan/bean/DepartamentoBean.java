package com.ctbli9.valorplan.bean;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
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
import com.ctbli9.valorplan.enumeradores.TipoRecurso;
import com.ctbli9.valorplan.modelo.Departamento;
import com.ctbli9.valorplan.modelo.FiltroDepartamento;
import com.ctbli9.valorplan.modelo.orc.Recurso;
import com.ctbli9.valorplan.negocio.DepartamentoArvoreService;
import com.ctbli9.valorplan.negocio.DepartamentoService;
import com.ctbli9.valorplan.negocio.EquipeService;
import com.ctbli9.valorplan.negocio.FuncionarioService;
import com.ctbli9.valorplan.recursos.Global;

import ctbli9.recursos.DataUtil;
import ctbli9.recursos.FacesMessages;
import com.ctbli9.valorplan.recursos.LibUtilFaces;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;


@ManagedBean(name="departamentoBean")
@ViewScoped
public class DepartamentoBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<Departamento> listaDepto = new ArrayList<Departamento>();
	private Departamento deptoPai;
	private Departamento deptoSel;
	private boolean nova;
	private List<Recurso> listaRecurso;
	private FiltroDepartamento filtro;
	private String arvore;
	
	private int nroAnoOrigem;
	private int nroAnoDestino;
	
	private StreamedContent file;
	private String nomeXLS;

	private MesAnoOrcamento anoReferencia = new MesAnoOrcamento();
	
	private FacesMessages msg;
	
	/*
	 * Construtor
	 */
	public DepartamentoBean() {
		this.msg = new FacesMessages();
		this.filtro = new FiltroDepartamento();
		this.deptoSel = new Departamento();

		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			this.listaRecurso = new EquipeService(con.getConexao()).listarRecursos(TipoRecurso.G);
			this.anoReferencia.setAno(PlanoServiceDAO.getPlanoSelecionado().getNrAno());
				
			listarPrimeiroNivel();

		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);		
		} 		
	}
	
	/*
	 * Atributos
	 */
	public List<Departamento> getListaDepto() {
		return listaDepto;
	}
	public Departamento getDeptoPai() {
		return deptoPai;
	}
	public void setDeptoPai(Departamento deptoPai) {
		this.deptoPai = deptoPai;
	}
	
	public Departamento getDeptoSel() {
		return deptoSel;
	}
	public void setDeptoSel(Departamento deptoSel) {
		this.deptoSel = deptoSel;
	}
	
	public boolean isNova() {
		return nova;
	}
	
	public List<Recurso> getListaRecurso() {
		return listaRecurso;
	}

	public FiltroDepartamento getFiltro() {
		return filtro;
	}
	public void setFiltro(FiltroDepartamento filtro) {
		this.filtro = filtro;
	}
	
	public String getArvore() {
		return arvore;
	}
	
	public int getNroAnoOrigem() {
		return nroAnoOrigem;
	}
	public void setNroAnoOrigem(int nroAnoOrigem) {
		this.nroAnoOrigem = nroAnoOrigem;
	}
	public int getNroAnoDestino() {
		return nroAnoDestino;
	}
	public void setNroAnoDestino(int nroAnoDestino) {
		this.nroAnoDestino = nroAnoDestino;
	}
	
	public MesAnoOrcamento getAnoReferencia() {
		return anoReferencia;
	}
	public void setAnoReferencia(MesAnoOrcamento anoReferencia) {
		this.anoReferencia = anoReferencia;
	}
	
	
	/*
	 * Operações
	 */
	
	public void listarPrimeiroNivel() {
		this.deptoPai = null;
		listarDepartamentoFilho();
	}
	
	
	public List<String> completarNomeFunc(String consulta) {
		List<String> listaNomes = null;
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			listaNomes = new FuncionarioService(con.getConexao()).retornarNomes(consulta);
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);		
		} 
		
		return listaNomes;
	}

	public void inicializarRegistro() {
		this.deptoSel = new Departamento();
		this.deptoSel.setNrAno(this.anoReferencia.getAno());
		this.deptoSel.setDepartamentoPai(this.deptoPai);
		this.deptoSel.setResponsavel(new Recurso(new MesAnoOrcamento()));

		if (this.deptoSel.getDepartamentoPai() == null)
			this.deptoSel.setDepartamentoPai(new Departamento());

		this.nova = true;
	}
	
	public void alterarRegistro() {
		this.nova = false;
		
		if (this.deptoSel.getDepartamentoPai() == null)
			this.deptoSel.setDepartamentoPai(new Departamento());
	}

	public void listarDepartamentoFilho() {
		if (this.deptoPai == null)
			this.deptoPai = new Departamento();
		
		this.filtro.setDeptoPai(this.deptoPai.getCdDepartamento());
		
		this.arvore = "";
		if (this.deptoPai.getNivel() > 0) {
			
			this.arvore = String.format("%02d.%s", this.deptoPai.getNivel(), this.deptoPai.getSgDepartamento());
			Departamento depAux = this.deptoPai.getDepartamentoPai();   // Vai para o próximo
			while (depAux != null) {
				this.arvore = String.format("%02d.%s &#10233; %s", depAux.getNivel(), depAux.getSgDepartamento(), this.arvore);
				depAux = depAux.getDepartamentoPai();
			}
		}		
		
		listarDepartamento();
	}
	
	public void listarDepartamentoAnterior() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
		
			if (this.deptoPai.getDepartamentoPai() != null)
				this.deptoPai = new DepartamentoService(con).pesquisarDepartamento(this.deptoPai.getDepartamentoPai().getCdDepartamento());
			else
				this.deptoPai = new Departamento();
			
			this.filtro.setDeptoPai(this.deptoPai.getCdDepartamento());
						
			this.arvore = "";
			if (this.deptoPai.getNivel() > 0) {
				this.arvore = String.format("%02d.%s", this.deptoPai.getNivel(), this.deptoPai.getSgDepartamento());
				Departamento depAux = this.deptoPai.getDepartamentoPai();   // Vai para o próximo
				while (depAux != null) {
					this.arvore = String.format("%02d.%s >> %s", depAux.getNivel(), depAux.getSgDepartamento(), this.arvore);
					depAux = depAux.getDepartamentoPai();
				}
			}
			
			listarDepartamento();
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		} 
	}
	
	private void listarDepartamento() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			this.filtro.setNroAno(this.anoReferencia.getAno());
			this.listaDepto = new DepartamentoService(con).listar(this.filtro);
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		} 
	}
	
	public boolean isItemSelecionado() {
		return deptoSel != null && deptoSel.getCdDepartamento() != 0;
	}

	
	public void salvar(ActionEvent event) {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
		
			if (this.nova) 
				new DepartamentoService(con).incluir(this.deptoSel);
			else 
				new DepartamentoService(con).alterar(this.deptoSel);
			ConexaoDB.gravarTransacao(con);
			
			msg.info("Departamento gravado com sucesso!");
			
			this.filtro.setDeptoPai(this.deptoPai.getCdDepartamento());
			listarDepartamento();
			
			LibUtilFaces.atualizarView("frm:cadDataTable", "frm:messages");
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
			if (e.getClass().getSimpleName().equalsIgnoreCase("RegraNegocioException"))
				FacesContext.getCurrentInstance().validationFailed();
			else
				LibUtilFaces.atualizarView("frm:messages");
				
		} finally {
			ConexaoDB.close(con);		
		}		
	}//end salvar
	
	
	public void excluir() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			new DepartamentoService(con).excluir(this.deptoSel);
			ConexaoDB.gravarTransacao(con);
			
			this.filtro.setDeptoPai(this.deptoPai.getCdDepartamento());
			listarDepartamento();
			this.deptoSel = null;
			
			msg.info("Departamento excluído!");
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
			if (e.getClass().getSimpleName().equalsIgnoreCase("RegraNegocioException"))
				FacesContext.getCurrentInstance().validationFailed();
				
		} finally {
			ConexaoDB.close(con);		
		}	
	}//excluir
	
	public void replicar() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
		
			new DepartamentoService(con).replicarDadosAno(nroAnoOrigem, nroAnoDestino);
			
			msg.info("Replicação efetuada com sucesso!<br/>"
					+ "<b>Ano Origem: </b> " + nroAnoOrigem 
					+ " <b>Ano Destino: </b> " + nroAnoDestino);
			
			LibUtilFaces.atualizarView("frm:messages");
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
			if (e.getClass().getSimpleName().equalsIgnoreCase("RegraNegocioException"))
				FacesContext.getCurrentInstance().validationFailed();
			else
				LibUtilFaces.atualizarView("frm:messages");
				
		} finally {
			ConexaoDB.close(con);		
		}
	}
	
	
	public void baixarPlanilha() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			String caminho = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
			this.nomeXLS = "ARVORE_" + DataUtil.dataString(new Date(), "yyyy_MM_dd_HH_mm_ss") + ".csv";
			caminho = caminho + "relatorios/" + this.nomeXLS;
			
			new DepartamentoArvoreService(con).gerarPlanilhaCSV(caminho);
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
			
		} finally {
			ConexaoDB.close(con);	
		}
	}
	

	public StreamedContent getFile() throws FileNotFoundException {
		String caminhoWebInf = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
		caminhoWebInf = caminhoWebInf + "relatorios/" + this.nomeXLS;
		InputStream stream = new FileInputStream(caminhoWebInf); //Caminho onde está salvo o arquivo.
        
        file = DefaultStreamedContent.builder()
                .name(this.nomeXLS)
                .contentType("application/csv")
                .stream(() -> stream)
                .build(); 
		
		return file;  
    }
	
}
