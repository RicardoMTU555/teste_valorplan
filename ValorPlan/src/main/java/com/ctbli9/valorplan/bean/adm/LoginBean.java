package com.ctbli9.valorplan.bean.adm;

import java.io.IOException;
import java.io.Serializable;

import javax.el.ELContext;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.primefaces.PrimeFaces;

import com.ctbli9.valorplan.recursos.Global;
import com.ctbli9.valorplan.recursos.LibUtilFaces;

import ctbli9.adm.modelo.CoUsuario;
import ctbli9.adm.modelo.Globais;
import ctbli9.adm.modelo.RetornoPadrao;
import ctbli9.adm.modelo.RetornoUsuario;
import ctbli9.adm.services.LoginServices;
import ctbli9.recursos.FacesMessages;
import ctbli9.recursos.LibUtil;

@ManagedBean(name="loginBean")
@ViewScoped
public class LoginBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private CoUsuario usuario;
	private String novaSenha1;
	private String novaSenha2;
	
	private FacesMessages msg;
	
	//@ManagedProperty(value="#{sistemaBean}")
	//private SistemaBean sistemaBean;
	
	/*
	 * Construtor
	 */
	public LoginBean() {
		this.usuario = new CoUsuario();
		this.msg = new FacesMessages();
	}
	
	/*
	 * Atributos
	 */
	public CoUsuario getUsuario() {
		return usuario;
	}
	public void setUsuario(CoUsuario usuario) {
		this.usuario = usuario;
	}
	
	public String getNovaSenha1() {
		return novaSenha1;
	}
	public void setNovaSenha1(String novaSenha1) {
		this.novaSenha1 = novaSenha1;
	}
	
	public String getNovaSenha2() {
		return novaSenha2;
	}
	public void setNovaSenha2(String novaSenha2) {
		this.novaSenha2 = novaSenha2;
	}
	
	public CoUsuario getUsuarioLogado() {
		return LibUtil.getUsuarioSessao();
	}
	
	public boolean isAdministrador() {
		return LibUtil.getUsuarioSessao().isIdtAdmin();
	}
	
	/*
	 * Metodos
	 */	
	public void obtemArea() {
		HttpServletRequest request = (HttpServletRequest) FacesContext
				.getCurrentInstance().getExternalContext().getRequest();
		
		String area = request.getParameter("area");
		
		request = null;
		
		if (Globais.AREA.isEmpty() || (!Globais.AREA.equals(area))) {
			Globais.defineUrl(area);
		}
	}	
	
	public String validarAcesso(String sistema) {
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("planoBean");
		
		//REGINALDO: Gambiarra para não ficar tendo que informar dados de acesso toda hora.
		FacesContext fc = FacesContext.getCurrentInstance();
		ServletContext sc = (ServletContext)fc.getExternalContext().getContext();
		String caminho = sc.getRealPath("/");
		caminho = caminho.substring(3);
		if(caminho.startsWith("RMC\\Desenv\\")) {
			System.out.println(caminho);
			if(usuario.getLogUsuario().isEmpty()) {
				//usuario.setLogUsuario("rcosta;marcelo.mendonca"); //leandro.souza
				//usuario.setLogUsuario("adm.evolracing");
				usuario.setLogUsuario("mpedrotti.agrinorte");
				//usuario.setLogUsuario("admin.colequip");
				//usuario.setLogUsuario("rchavans.colequip");
				usuario.setSenUsuario("0000");	
			}
		}
		//REGINALDO: FIM Gambiarra
		RetornoUsuario retorno = new RetornoUsuario();
		retorno.setUsuario(this.usuario);
		
		String loginUsuario[] = usuario.getLogUsuario().split(";");
		if ( beanSistema().validarUsuarioLogado(loginUsuario[0]) ) {
			retorno = new LoginServices().validarLogin(retorno, sistema);
			this.usuario = retorno.getUsuario();			
		} else
			retorno.getMensagem().setMensagemTexto("Usuário já logado em outra estação ou navegador.");
		
		
		if (!retorno.getMensagem().isErro()) {
			beanSistema().adicionar();
			Global.setFuncionarioLogado();
			return "home.xhtml";
		}
		else {
			this.msg.erro(retorno.getMensagem().getMensagem());
			return "";
		}
	}
	
	public void salvarSenha(String sistema) {
		RetornoPadrao resposta = new LoginServices().validarSenha(usuario, sistema, novaSenha1, novaSenha2);
		
		if (!resposta.isErro()) {
			msg.info(resposta.getMensagem());
			LibUtilFaces.atualizarView("frm:msg");			
			
		} else {
			msg.erro(resposta.getMensagem());
			FacesContext.getCurrentInstance().validationFailed();
		}
	}
	
	public void validarPermissao(String programa) {
		if (!new LoginServices().validarPermissao(programa)) {
			ExternalContext context1 = FacesContext.getCurrentInstance().getExternalContext();
			
			ServletContext sc = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
			String raiz = sc.getRealPath("/");
			sc = null;
			if (raiz.endsWith("\\")) {
				raiz = raiz.substring(0, raiz.length() - 1);
				raiz = raiz.substring(raiz.lastIndexOf("\\") + 1);

			} else if (raiz.endsWith("/")) {
				raiz = raiz.substring(0, raiz.length() - 1);
				raiz = raiz.substring(raiz.lastIndexOf("/") + 1);
			}


			
			String URL = "/" + raiz.toLowerCase() + "/login.xhtml";

			try {
				context1.redirect(URL);
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}//if
	}//validarPermissao
	
	
	public boolean bloquearAcesso(String programa) {
		return !(new LoginServices().validarPermissao(programa));
	}//bloquearAcesso
	
	
	public void manual() {
		ServletContext sc = (ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext();
		String URL = sc.getContextPath() + "/relatorios/ValorPlan-Manual_do_Usuario.pdf";
		sc = null;
		
		URL = "window.open('" + URL + "', '_blank')";
		PrimeFaces.current().executeScript(URL);
	}//
	
	public String getInicio() {
		System.out.println("ESTOU EM INICIO");
		ServletContext sc = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
		String raiz = sc.getRealPath("/");
		sc = null;
		if (raiz.endsWith("\\")) {
			raiz = raiz.substring(0, raiz.length() - 1);
			raiz = raiz.substring(raiz.lastIndexOf("\\") + 1);

		} else if (raiz.endsWith("/")) {
			raiz = raiz.substring(0, raiz.length() - 1);
			raiz = raiz.substring(raiz.lastIndexOf("/") + 1);
		}

		return "/" + raiz.toLowerCase() + "/home.xhtml";
	}
	
	public void logout() {
		ServletContext sc = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
		String raiz = sc.getRealPath("/");
		if (raiz.endsWith("\\")) {
			raiz = raiz.substring(0, raiz.length() - 1);
			raiz = raiz.substring(raiz.lastIndexOf("\\") + 1);

		} else if (raiz.endsWith("/")) {
			raiz = raiz.substring(0, raiz.length() - 1);
			raiz = raiz.substring(raiz.lastIndexOf("/") + 1);
		}
		
		beanSistema().subtrair();
		
		LibUtil.finalizaSessao();
		
		ExternalContext context1 = FacesContext.getCurrentInstance().getExternalContext();
		
		String URL = "/" + raiz.toLowerCase() + "/login.xhtml";
		
		try {
			context1.redirect(URL);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}//logout


	public SistemaBean beanSistema() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ELContext elContext = facesContext.getELContext();
		SistemaBean bean = (SistemaBean) facesContext.getApplication().getELResolver().getValue(elContext, null, "sistemaBean");
		return bean;
	}

}
