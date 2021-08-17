package com.ctbli9.valorplan.bean;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.model.TreeNode;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.CentroCusto;
import com.ctbli9.valorplan.modelo.Departamento;
import com.ctbli9.valorplan.modelo.orc.Recurso;
import com.ctbli9.valorplan.negocio.DepartamentoArvoreService;
import com.ctbli9.valorplan.negocio.EquipeService;
import com.ctbli9.valorplan.recursos.Global;

import ctbli9.recursos.FacesMessages;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

@ManagedBean(name="depArvoreBean")
@ViewScoped
public class DepartamentoArvoreBean implements Serializable {
     
    private static final long serialVersionUID = 1L;
    
    private MesAnoOrcamento anoReferencia = new MesAnoOrcamento();
	
	private TreeNode root1;
    private TreeNode selectedNode;
     
    private CentroCusto cencus;
    private Departamento depto;
    
	private TreeNode estrut;
    
    private FacesMessages msg = new FacesMessages();
    private boolean seleciona;
    
    /*
    // REGINALDO: UM OUTRO EXEMPLO PARA INSTANCIAR CLASSE DE SERVIÇO
    @ManagedProperty("#{documentService}")
    private DepartamentoArvoreService service;
     */
    // REGINALDO: anotação faz o método ser executado após o instanciamento do bean
    @PostConstruct
    public void init() {
    	this.estrut = null;
    	this.seleciona = false;
    }

    /*
     * Atributos
     */
    
    public MesAnoOrcamento getAnoReferencia() {
		return anoReferencia;
	}
    public void setAnoReferencia(MesAnoOrcamento anoReferencia) {
		this.anoReferencia = anoReferencia;
	}
    
	public TreeNode getRoot1() {
        return root1;
    }
    
    public TreeNode getEstrut() {
		return estrut;
	}
 
    public TreeNode getSelectedNode() {
        return selectedNode;
    }
 
    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
    }
    
    public CentroCusto getCencus() {
		return cencus;
	}
    
    public Departamento getDepto() {
		return depto;
	}
    
    public boolean isSeleciona() {
		return seleciona;
	}
    
    
    /*
     * Métodos
     */
    
    /*
     * Utilizada para visualizar a estrutura organizacional da empresa graficamente. 
     */
    public void carregarArvore() {
    	ConexaoDB con = null;
    	try {
			con = new ConexaoDB();
	        root1 = new DepartamentoArvoreService(con).montaArvoreEmpresaSemRaiz(this.anoReferencia.getAno());
	        
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		}
    }

    /*
     * Utilizado no cadastro de centro de custo para ligar um setor na sua árvore
     */
    public void carregarArvoreDepto(int nroAno, int codDepartamento, String acao) {
    	ConexaoDB con = null;
    	try {
			con = new ConexaoDB();
	    	this.seleciona = acao.equals("SELECIONA");
    		root1 = new DepartamentoArvoreService(con).montaArvoreEmpresa(nroAno, codDepartamento);
        	
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		}
    }
    
    
    public String getItemSelecionado() {
		if (this.selectedNode != null) {
			if (this.selectedNode.getData().getClass().getName().indexOf("CentroCusto") > -1) {
				return "SET";
			} else {
				if (this.selectedNode.getChildCount() == 0 || 
						this.selectedNode.getChildren().get(0).getData().getClass().getName().indexOf("CentroCusto") > -1) {
					return "DEP";
				} else
					return "";
			}	
		} else
			return "";
	}

	public void prepararSelecao() {
		if (this.selectedNode.getData().getClass().getName().indexOf("CentroCusto") > -1) {
			this.depto = null;
           	this.cencus = (CentroCusto) this.selectedNode.getData();
		} else {
			this.cencus = null;
			if (this.selectedNode.getData().getClass().getName().indexOf("Departamento") > -1) {
				this.depto = (Departamento) this.selectedNode.getData();
			}
		}	
	}	
    
    public void MostraSelecionado() {
        if(selectedNode != null) {
        	
        	ConexaoDB con = null;
    		try {
    			con = new ConexaoDB();
    		     if (selectedNode.getData().getClass().getName().indexOf("CentroCusto") > -1) {
	        		this.depto = null;
	            	this.cencus = (CentroCusto) selectedNode.getData();
	            	if (this.cencus.getResponsavel() != null && this.cencus.getResponsavel().getCdRecurso() > 0)
	            		this.cencus.setResponsavel(new EquipeService(con.getConexao()).
	            				getRecurso(this.cencus.getResponsavel().getCdRecurso()));
	            	else
	            		this.cencus.setResponsavel(new Recurso(new MesAnoOrcamento()));
	            } else {
	            	this.cencus = null;
	        		this.depto = (Departamento) selectedNode.getData();
	        		if (!this.depto.getSgDepartamento().equals("EMPRESA"))
	        			this.depto.setResponsavel(new EquipeService(con.getConexao()).
	            				getRecurso(this.depto.getResponsavel().getCdRecurso()));
	        		else
	        			this.depto.setResponsavel(new Recurso(new MesAnoOrcamento()));
	            }
        	} catch (Exception e) {
    			Global.erro(con, e, msg, null);
    		} finally {
    			ConexaoDB.close(con);		
    		} 
            
        }
    }   

    public void criarEstruturaDepartamentos() {
    	arvoreDoGestor(false, false);
    }
    
    public void criarEstruturaCompleta() {
    	arvoreDoGestor(true, false);
    }
    
    public void criarEstruturaReceita() {
    	arvoreDoGestor(true, true);
    }
    
    private void arvoreDoGestor(boolean mostraRecursos, boolean mostraSoProdutivo) {
    	
    	ConexaoDB con = null;
    	try {
			con = new ConexaoDB();
	        this.estrut = new DepartamentoArvoreService(con).arvoreSetoresDoGestor(mostraRecursos, mostraSoProdutivo);
	        montaCodigoEquipe(this.estrut);
	        	        
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		}
    }
    
    
    
    
    private String montaCodigoEquipe(TreeNode elo) {
		String codigoEquipeVenda = "";
		
		for (TreeNode filho : elo.getChildren()) {
			if (filho.getChildCount() > 0) {
				String codAux = montaCodigoEquipe(filho);
				
				if (!codAux.isEmpty()) {
					if (codigoEquipeVenda.isEmpty())
						codigoEquipeVenda += codAux;
					else
						codigoEquipeVenda += "," + codAux;
				}
				
			} else {
				if (filho.getData().getClass().getName().indexOf("Recurso") > -1) {
					Recurso func = (Recurso) filho.getData();
					if (codigoEquipeVenda.isEmpty())
						codigoEquipeVenda += func.getCdRecurso();
					else
						codigoEquipeVenda += "," + func.getCdRecurso();
					
					func.setCodEquipeVenda(codigoEquipeVenda);
				}	
			}
		}// for
		
		
		if (elo.getData().getClass().getName().indexOf("CentroCusto") > -1) {
           	this.cencus = (CentroCusto) elo.getData();
           	if (this.cencus.getResponsavel() != null)
           		this.cencus.getResponsavel().setCodEquipeVenda(codigoEquipeVenda);
           	
		} else if (elo.getData().getClass().getName().indexOf("Departamento") > -1) {
       		this.depto = (Departamento) elo.getData();
       		if (this.depto.getResponsavel() != null)
       			this.depto.getResponsavel().setCodEquipeVenda(codigoEquipeVenda);
       		
		}
		
		return codigoEquipeVenda;
	}
    
}