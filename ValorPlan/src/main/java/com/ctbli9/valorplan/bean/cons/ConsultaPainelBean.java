package com.ctbli9.valorplan.bean.cons;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.model.TreeNode;
import org.primefaces.model.charts.bar.BarChartModel;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.CentroCusto;
import com.ctbli9.valorplan.modelo.ResumoDRE;
import com.ctbli9.valorplan.negocio.consulta.ConsultaService;
import com.ctbli9.valorplan.recursos.Global;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

import ctbli9.recursos.FacesMessages;
import ctbli9.recursos.RegraNegocioException;

@ManagedBean(name = "consultaPainelBean")
@ViewScoped
public class ConsultaPainelBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private ConexaoDB con = null;
	private ConsultaService servico;
	
	private MesAnoOrcamento mesRef = new MesAnoOrcamento();
	private List<ResumoDRE> contasReceitas = new ArrayList<>();
	private BarChartModel receitasChart;
	
	private CentroCusto cenCusto;
	
	private FacesMessages msg = new FacesMessages();

	/*
	 * Contrutor
	 */	
    @PostConstruct
    public void init() {
    	
    	try {
			con = new ConexaoDB();
			if (Global.getFuncionarioLogado() != null) {
				servico = new ConsultaService(con);
			}
			ConexaoDB.gravarTransacao(con);
			
		} catch (RegraNegocioException e) {
			ConexaoDB.desfazerTransacao(con);			
		} catch (Exception e) {
			ConexaoDB.desfazerTransacao(con);
			e.printStackTrace();
		} //finally {
			//ConexaoDB.close(con);
		//}
    }
    
    @PreDestroy
    public void fechar() {
    	servico = null;
    	ConexaoDB.close(con);
    	con = null;
    }

	
	/*
	 * Atributos
	 */
    public MesAnoOrcamento getMesRef() {
		return mesRef;
	}
    public void setMesRef(MesAnoOrcamento mesRef) {
		this.mesRef = mesRef;
	}
	public List<ResumoDRE> getContasReceitas() {
		return contasReceitas;
	}
	public BarChartModel getReceitasChart() {
        return receitasChart;
    }

	
	/*
	 * Metodos
	 */
	
	public void listar(TreeNode elo) {
		this.cenCusto = new CentroCusto();
		
		if(elo != null) {
			if (elo.getData().getClass().getName().indexOf("CentroCusto") > -1) {
				this.cenCusto = (CentroCusto) elo.getData(); 
				listar();
			}
		}
	}

	public void listar() {
		if (this.cenCusto.getCdCentroCusto() > 0) { 
			try {
				this.contasReceitas = servico.listarReceitasPorConta(this.cenCusto, this.mesRef);
				receitasChart = servico.criarGraficoReceita(this.contasReceitas);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public void mesAnterior() {
		int anoAux = this.mesRef.getAno();
		this.mesRef.sub();
		if (this.mesRef.getAno() == anoAux)
			listar();
		else
			this.mesRef.add();
	}

	public void mesPosterior() {
		int anoAux = this.mesRef.getAno();
		this.mesRef.add();
		if (this.mesRef.getAno() == anoAux)
			listar();
		else
			this.mesRef.sub();
	}
	
}
