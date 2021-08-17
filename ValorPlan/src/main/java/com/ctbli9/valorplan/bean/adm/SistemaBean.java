package com.ctbli9.valorplan.bean.adm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.servlet.http.HttpSession;

import ctbli9.adm.modelo.CoUsuario;
import ctbli9.recursos.LibUtil;

@ManagedBean(name="sistemaBean")
@ApplicationScoped
public class SistemaBean implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private List<HttpSession> sessoesAtivas;
	
	/*
	 * Construtor
	 */
	public SistemaBean() {
		this.sessoesAtivas = new ArrayList<HttpSession>();
		System.out.println("INSTANCIEI O BEAN. Valor atual: " + getQuantUsuario());
	}
	
	/*
	 * Atributos
	 */
	public List<HttpSession> getSessoesAtivas() {
		return sessoesAtivas;
	}
	public void setSessoesAtivas(List<HttpSession> sessoesAtivas) {
		this.sessoesAtivas = sessoesAtivas;
	}
	public int getQuantUsuario() {
		return this.sessoesAtivas.size();
	}
	
	
	/*
	 * Metodos
	 */
	public void adicionar() {
		sessoesAtivas.add(LibUtil.getSessao());
		verificaSessoesInvalidas();
	}
	
	public void subtrair() {
		removeSessao(LibUtil.getSessao().getId());
	}
	
	public boolean validarUsuarioLogado(String usuario) {
		
		return true;
	}
	
	public String[] getUsuariosLogados() {
		verificaSessoesInvalidas();
		
		String[] lista = new String[getQuantUsuario()];
		int i = 0;
		for (HttpSession sess : sessoesAtivas) {
			try { 
				long resultado = (System.currentTimeMillis() - sess.getCreationTime()) / 1000; // Resultado em segundos
				double valorDecimal = resultado / 60.00;
				long resInteiro = (long) valorDecimal;
				long resDecimal = (long)Math.round((valorDecimal - (long)valorDecimal) * 60); //(long)(valorDecimal - (long)valorDecimal);
				
				lista[i++] = String.format("%s  ==> Conectado a %02d:%02d minutos.", 
						((CoUsuario) sess.getAttribute("usuario")).getLogUsuario(),
						resInteiro,
						resDecimal);
			} catch (IllegalStateException e) {}
		}//for
		
		
		return lista;
	}

	

	/*
	 * Metodos PRIVADOS: uso interno da classe
	 */
	
	private void verificaSessoesInvalidas() {
		
		List<String> sessoesInvalidas = new ArrayList<String>();
		
		for (HttpSession sess : sessoesAtivas) {
			try { 
				/*CoUsuario usuarioLogado = (CoUsuario) sess.getAttribute("usuario");
				usuarioLogado = null;
				*/
				sess.getAttribute("usuario");
				
			} catch (IllegalStateException e) {
				sessoesInvalidas.add(sess.getId());
			}
		}//for
		
		if (sessoesInvalidas.size() > 0) {
			for (String nroId : sessoesInvalidas) {
				removeSessao(nroId);
			}
		}//if
	}

	private void removeSessao(String nroId) {
		for (HttpSession sess : sessoesAtivas) {
			if (sess.getId().equals(nroId)) {
				sessoesAtivas.remove(sess);
				break;
			}
		}
	}
	
}
