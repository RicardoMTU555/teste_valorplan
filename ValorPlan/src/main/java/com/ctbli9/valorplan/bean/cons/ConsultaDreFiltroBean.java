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

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.CentroCusto;
import com.ctbli9.valorplan.modelo.Departamento;
import com.ctbli9.valorplan.modelo.FiltroCentroCusto;
import com.ctbli9.valorplan.modelo.ResumoDRE;
import com.ctbli9.valorplan.negocio.CentroCustoService;
import com.ctbli9.valorplan.negocio.DepartamentoService;
import com.ctbli9.valorplan.negocio.ResumoService;
import com.ctbli9.valorplan.recursos.Global;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

import ctbli9.modelo.Filial;
import ctbli9.negocio.FilialService;
import ctbli9.recursos.FacesMessages;
import ctbli9.recursos.LibUtil;

@ManagedBean(name = "consultaDreFiltroBean")
@ViewScoped
public class ConsultaDreFiltroBean implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private List<Filial> filiais;
	private Filial filial;
	private List<Departamento> listaAreas;
    private List<String> areasSelecionadas;

	private List<CentroCusto> listaSetores;
    private List<String> setoresSelecionados;
    private boolean selectAllDepto;
    private boolean selectAllSetor;
    
    private MesAnoOrcamento anoMes;
	private TreeNode root1;
	private TreeNode selectedNode;
	
	private FacesMessages msg = new FacesMessages();
	
	private boolean podeBaixar;
	private StreamedContent file;
	private String nomeXLS;
	
	private int tipo;

	/*
	 * Construtor
	 */
	public ConsultaDreFiltroBean() {
		this.podeBaixar = false;
		this.anoMes = new MesAnoOrcamento();
		
		ResumoDRE resumo = new ResumoDRE(12);
		resumo.setDescricao("RAIZ");
		this.root1 = new DefaultTreeNode(resumo, null);
		
		ConexaoDB con = null;
    	try {
    		con = new ConexaoDB();
			this.filiais = new FilialService(con.getConexao()).listar();
			this.filial = new Filial();
			
			selecionarArea();
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		}
	}
	
	public void habilitarDownload() {
		this.podeBaixar = true;
	}
	
	/*
	 * Atributos
	 */
	public List<Filial> getFiliais() {
		return filiais;
	}
	public Filial getFilial() {
		return filial;
	}
	public void setFilial(Filial filial) {
		this.filial = filial;
	}
	
	
	public List<Departamento> getListaAreas() {
		return listaAreas;
	}
	
	public List<String> getAreasSelecionadas() {
		return areasSelecionadas;
	}
	public void setAreasSelecionadas(List<String> areasSelecionadas) {
		this.areasSelecionadas = areasSelecionadas;
	}
	
	public List<CentroCusto> getListaSetores() {
		return listaSetores;
	}
	public List<String> getSetoresSelecionados() {
		return setoresSelecionados;
	}
	public void setSetoresSelecionados(List<String> setoresSelecionados) {
		this.setoresSelecionados = setoresSelecionados;
	}
	
	public boolean isSelectAllDepto() {
		return selectAllDepto;
	}
	public void setSelectAllDepto(boolean selectAllDepto) {
		this.selectAllDepto = selectAllDepto;
	}
	
	public boolean isSelectAllSetor() {
		return selectAllSetor;
	}
	public void setSelectAllSetor(boolean selectAllSetor) {
		this.selectAllSetor = selectAllSetor;
	}	
	
	public MesAnoOrcamento getAnoMes() {
		return anoMes;
	}
	public void setAnoMes(MesAnoOrcamento anoMes) {
		this.anoMes = anoMes;
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
    
    public boolean isExcelGerado() {
    	return this.podeBaixar;
    }
    
    public int getPosicaoMes() {
    	return this.anoMes.getMes() - 1;
    }

    // TODO Reginaldo
    public void baixarPlanilhaEstrutura() {
		ConexaoDB con = null;
    	try {
    		con = new ConexaoDB();
			
			String caminho = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
			this.nomeXLS = "PLAN_"+ LibUtil.getUsuarioSessao().getLogUsuario()  +".xls";
			caminho = caminho + "relatorios/" + this.nomeXLS;
			
			//new ResumoService(con).gerarPlanilhaSaldos(caminho, false);
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} 
	}
	
    public void baixarPlanilhaLinear() {
		ConexaoDB con = null;
    	try {
    		con = new ConexaoDB();
			
			String caminho = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
			this.nomeXLS = "PLAN_"+ LibUtil.getUsuarioSessao().getLogUsuario()  +".xls";
			caminho = caminho + "relatorios/" + this.nomeXLS;
			
			if (tipo == 2)
				this.anoMes.setMes(12);
			
			int[] setoresAux = montarSetoresSelecionados();
			
			new ResumoService(con).gerarPlanilhaSaldosSpl(caminho, tipo, this.anoMes, this.filial, setoresAux);
			
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
    				
			int[] setoresAux = montarSetoresSelecionados();
			
			if (tipo == 2)
				this.anoMes.setMes(12);
			
			this.root1 = new ResumoService(con).listarResumoFiltroDRE(tipo, this.anoMes, this.filial, setoresAux);

			// REGINALDO: forma de chamar um javascript de dentro do bean
			//RequestContext.getCurrentInstance().execute("PF('accordian-widgetVar').unselect(index)");
			
			this.podeBaixar = true;
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		}
	}//listarDRE

	private int[] montarSetoresSelecionados() {
		int[] setoresAux = new int[this.setoresSelecionados.size()];
		
		int i = 0;
		for (String setor : this.setoresSelecionados) {
			setoresAux[i++] = Integer.parseInt(setor);
		}
		return setoresAux;
	}
    
    
	public void selecionarArea() {
		ConexaoDB con = null;
    	try {
    		con = new ConexaoDB();
			listaAreas = new DepartamentoService(con).listarAreas(0);
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		}
	}
	
	public void selecionarSetores() {
		
		if (this.areasSelecionadas.size() > 0) {
			ConexaoDB con = null;
	    	try {
	    		con = new ConexaoDB();
				
				FiltroCentroCusto filtroCC = new FiltroCentroCusto();
				
				int[] areaAux = new int[this.areasSelecionadas.size()];
				for (int i = 0; i < this.areasSelecionadas.size(); i++) {
					areaAux[i] = Integer.parseInt(this.areasSelecionadas.get(i));
					
				}
				
				filtroCC.setDepartamentos(areaAux);
				filtroCC.setSetorDoGestor(true);
				this.listaSetores = new CentroCustoService(con).listar(filtroCC);

			} catch (Exception e) {
				Global.erro(con, e, msg, null);
			} finally {
				ConexaoDB.close(con);
			}	
		} else {
			this.listaSetores = new ArrayList<CentroCusto>();
			this.setoresSelecionados = new ArrayList<>();
		}
		
	}
	
	public void processarDeptos() {
		if (this.selectAllDepto) {
			this.areasSelecionadas = new ArrayList<>();
			
			for (int i = 0; i < this.listaAreas.size(); i++) {
				this.areasSelecionadas.add(Long.toString(this.listaAreas.get(i).getCdDepartamento()));
			}
			
		} else {
			this.areasSelecionadas = new ArrayList<>();
		}
		selecionarSetores();	
	}
	
	public void processarSetores() {
		if (this.selectAllSetor) {
			this.setoresSelecionados = new ArrayList<>();
			
			for (int i = 0; i < this.listaSetores.size(); i++) {
				this.setoresSelecionados.add(Integer.toString(this.listaSetores.get(i).getCdCentroCusto()));
			}
			
		} else {
			this.setoresSelecionados = new ArrayList<>();
		}
	}
	
	
	
}
