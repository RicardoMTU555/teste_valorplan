package com.ctbli9.valorplan.bean.cons;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;

import com.ctbli9.valorplan.DAO.PlanoServiceDAO;
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.ResumoDRE;
import com.ctbli9.valorplan.negocio.DepartamentoService;
import com.ctbli9.valorplan.negocio.ResumoService;
import com.ctbli9.valorplan.recursos.Global;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

import ctbli9.modelo.Filial;
import ctbli9.negocio.FilialService;
import ctbli9.recursos.FacesMessages;
import ctbli9.recursos.LibUtil;

@ManagedBean(name = "consultaDreBean")
@ViewScoped
public class ConsultaDreBean implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private List<Filial> filiais;
	private List<String> filiaisSelecionadas;
	private boolean selectAllFilial;
	private int nivel;
	private MesAnoOrcamento anoMes;
	private boolean agrupaConta;
	private TreeNode root1;
	private TreeNode selectedNode;
    private ResumoDRE resDRE;
    private int[] lista;
	
	private FacesMessages msg = new FacesMessages();
	
	private boolean podeBaixar;
	private StreamedContent file;
	private String nomeXLS;

	private int tipo;
    
	/*
	 * Atributos
	 */
	public List<Filial> getFiliais() {
		return filiais;
	}
	public List<String> getFiliaisSelecionadas() {
		return filiaisSelecionadas;
	}
	public void setFiliaisSelecionadas(List<String> filiaisSelecionadas) {
		this.filiaisSelecionadas = filiaisSelecionadas;
	}

	public boolean isSelectAllFilial() {
		return selectAllFilial;
	}
	public void setSelectAllFilial(boolean selectAllFilial) {
		this.selectAllFilial = selectAllFilial;
	}
	
	public int getNivel() {
		return nivel;
	}
	public void setNivel(int nivel) {
		this.nivel = nivel;
	}
	
	public MesAnoOrcamento getAnoMes() {
		return anoMes;
	}
	public void setAnoMes(MesAnoOrcamento anoMes) {
		this.anoMes = anoMes;
	}
	
	public boolean isAgrupaConta() {
		return agrupaConta;
	}
	public void setAgrupaConta(boolean agrupaConta) {
		this.agrupaConta = agrupaConta;
	}
	
	public int[] getListaNiveisDepto() {
		return this.lista;
	}
	
    public TreeNode getRoot1() {
        return root1;
    }
    public void setRoot1(TreeNode root1) {
		this.root1 = root1;
	}
    
    public TreeNode getSelectedNode() {
		return selectedNode;
	}
    public void setSelectedNode(TreeNode selectedNode) {
		this.selectedNode = selectedNode;
	}
    
    public ResumoDRE getResDRE() {
		return resDRE;
	}
    public void setResDRE(ResumoDRE resDRE) {
		this.resDRE = resDRE;
	}
	
    public boolean isExcelGerado() {
    	return this.podeBaixar;
    }
    
    
    public int getPosicaoMes() {
    	return this.anoMes.getMes() - 1;
    }
    
    /*
     * Métodos
     */
    public void inicializar() {
    	this.agrupaConta = true;
		this.podeBaixar = false;
		this.anoMes = new MesAnoOrcamento();
		
		ResumoDRE resumo = new ResumoDRE(12);
		resumo.setDescricao("RAIZ");
		this.root1 = new DefaultTreeNode(resumo, null);
		
		ConexaoDB con = null;
    	try {
    		con = new ConexaoDB();
			this.filiais = new FilialService(con.getConexao()).listar();
									
			this.lista = new DepartamentoService(con).listarNiveisDepto(PlanoServiceDAO.getPlanoSelecionado().getNrAno());
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		}
	}

	
    public void habilitarDownload() {
		this.podeBaixar = true;
	}
    
    public void baixarPlanilhaEstrutura() {
		ConexaoDB con = null;
    	try {
			
			String caminho = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
			this.nomeXLS = "PLAN_Estrut" + LibUtil.getUsuarioSessao().getLogUsuario()  +".xls";
			caminho = caminho + "relatorios/" + this.nomeXLS;
			
			new ResumoService(con).gerarPlanilhaEstrutura(caminho, this.root1, this.tipo, this.anoMes);
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} 
	}
	
    public void baixarPlanilhaLinear() {
		ConexaoDB con = null;
    	try {
    		con = new ConexaoDB();
			
			String caminho = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
			this.nomeXLS = "PLAN_Linear"+ LibUtil.getUsuarioSessao().getLogUsuario()  +".xls";
			caminho = caminho + "relatorios/" + this.nomeXLS;
			
			new ResumoService(con).gerarPlanilhaSaldosMN(caminho, this.anoMes, this.nivel, 
					this.filiaisSelecionadas, this.agrupaConta);
			
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
	
	public void processarFiliais() {
		this.filiaisSelecionadas = new ArrayList<>();
		if (this.selectAllFilial) {
			this.filiais.forEach(f -> this.filiaisSelecionadas.add(Long.toString(f.getCdFilial())));
		} 
	}


	/*
	 * tipo:
	 *    1 = Mensal (um mês específico)
	 *    2 = Anual (todos os meses)
	 */
	public void listarDRE(int tipo) {
		this.tipo = tipo;
		ConexaoDB con = null;
    	try {
    		con = new ConexaoDB();
			if (tipo == 2)
				this.anoMes.setMes(12);
			
			this.root1 = new ResumoService(con).listarResumoDRE(this.anoMes, this.nivel, this.filiaisSelecionadas,
					this.agrupaConta);

			this.podeBaixar = true;
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		}
	}//listarDRE
    
}
