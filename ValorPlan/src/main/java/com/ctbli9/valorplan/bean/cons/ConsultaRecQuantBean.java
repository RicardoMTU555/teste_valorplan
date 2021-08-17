package com.ctbli9.valorplan.bean.cons;

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

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.ctbli9.valorplan.DAO.PlanoServiceDAO;
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.enumeradores.MedidaOrcamento;
import com.ctbli9.valorplan.modelo.receita.CategoriaReceita;
import com.ctbli9.valorplan.negocio.CategoriaReceitaService;
import com.ctbli9.valorplan.negocio.DepartamentoService;
import com.ctbli9.valorplan.negocio.ResumoService;
import com.ctbli9.valorplan.recursos.Global;

import ctbli9.recursos.DataUtil;
import ctbli9.recursos.FacesMessages;

@ManagedBean(name = "consultaRecQuantBean")
@ViewScoped
public class ConsultaRecQuantBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<CategoriaReceita> categorias = new ArrayList<CategoriaReceita>();
	private CategoriaReceita categoria;
	private int nivel;
	
	private FacesMessages msg = new FacesMessages();
	private StreamedContent file;
	private String nomeXLS;

	/*
	 * Contrutor
	 */	
	public ConsultaRecQuantBean() {
		this.categoria = new CategoriaReceita();
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			List<CategoriaReceita> categoriasAux = new CategoriaReceitaService(con).listarCategoriaReceita();
			
			this.categorias = new ArrayList<CategoriaReceita>(); 
			
			for (CategoriaReceita categoriaReceita : categoriasAux) {
				if (categoriaReceita.getMedida().equals(MedidaOrcamento.Q))
					this.categorias.add(categoriaReceita);
			}
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		}
		
	}
	
	/*
	 * Atributos
	 */
	public List<CategoriaReceita> getCategorias() {
		return categorias;
	}
	public CategoriaReceita getCategoria() {
		return categoria;
	}
	public void setCategoria(CategoriaReceita categoria) {
		this.categoria = categoria;
	}
	
	public int getNivel() {
		return nivel;
	}
	public void setNivel(int nivel) {
		this.nivel = nivel;
	}
	
	/*
	 * Metodos
	 */
	
	public int[] getListaNiveisDepto() {
		int[] lista = null;		
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
		
			lista = new DepartamentoService(con).listarNiveisDepto(PlanoServiceDAO.getPlanoSelecionado().getNrAno());
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		}
		return lista;
	}
	
	public void baixarPlanilhaGeral() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			String caminho = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
			this.nomeXLS = "QuantidadeReceitas_" + DataUtil.dataString(new Date(), "yyyy_MM_dd") + ".xls";
			caminho = caminho + "relatorios/" + this.nomeXLS;
			
			new ResumoService(con).gerarPlanilhaQuantRec(caminho, this.nivel, this.categoria);
			
			msg.info("Planilha gerada.");
			
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
	
}
