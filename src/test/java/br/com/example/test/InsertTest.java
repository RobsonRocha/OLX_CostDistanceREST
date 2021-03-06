package br.com.example.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import br.com.example.connection.DBConnection;
import br.com.example.pojo.WordDistance;
import br.com.example.test.mock.RestMock;
import br.com.example.test.util.Utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class InsertTest {
	
	private String randomWord;
	private static final String REST_URL = "http://localhost:8080/CostDistanceRest/main/"; 
	
	@AfterMethod
	private void deleteWord() throws Exception{
		StringBuilder sql = new StringBuilder();
		sql.append("delete from distancia.palavras where termo = ? ");
		Connection con = null;
		PreparedStatement ps = null;
		
		String newWord = randomWord
				.toUpperCase().trim();		
		try {
			
			con = DBConnection.getConnection();
			ps = con.prepareStatement(sql.toString());
			
			ps.setString(1, newWord);
			ps.executeUpdate();			
			
		} catch (SQLException e) {
			System.out.println("Erro com o banco de dados.");
			e.printStackTrace();
			throw new Exception("Erro com o banco de dados.");
		} finally {
			try {
				ps.close();
				con.close();
			} catch (SQLException e) {
				System.out.println("Erro ao fehar o banco de dados.");
				e.printStackTrace();
				throw new Exception("Erro ao fechar o banco de dados.");
			}

		}
	}
	
	@Test
	public void insertWordTest() throws Exception{
		RestMock rm = new RestMock();
		randomWord = Utils.randomWord();
		String answer = rm.insertWord(randomWord);
		List<WordDistance> wd = rm.getMinDistance(randomWord, 0);
		Assert.assertTrue(wd.get(0).getWord2().equals(randomWord.toUpperCase()));
		Assert.assertTrue(answer.contains("sucesso"));		
	}
	
	@Test(expectedExceptions=WebApplicationException.class)
	public void insertAWordThatAlreadyExistsTest() throws Exception{
		RestMock rm = new RestMock();
		randomWord = Utils.randomWord();
		rm.insertWord(randomWord);
		rm.insertWord(randomWord);		
	}
	
	@Test
	public void insertWordWithSpecialCharacterTest() throws Exception{
		RestMock rm = new RestMock();
		randomWord = Utils.randomWord();
		String randomWordWithoutSpecialCharacter = randomWord + "cao";
		randomWord +="çãó";
		String answer = rm.insertWord(randomWord);
		List<WordDistance> wd = rm.getMinDistance(randomWordWithoutSpecialCharacter, 0);
		randomWord = randomWordWithoutSpecialCharacter;
		Assert.assertTrue(answer.contains("sucesso"));
		Assert.assertEquals(wd.size(), 1);
		Assert.assertEquals(wd.get(0).getDistance(), 0);
		Assert.assertFalse(wd.get(0).getWord2().equals(randomWord));
		Assert.assertTrue(wd.get(0).getWord2().equals(randomWordWithoutSpecialCharacter.toUpperCase()));
		
	}
	
	//Para esses testes o serviço deve estar no ar.
	
	@Test
	public void insertWordUsingRestServiceTest() throws Exception {
		
		randomWord = Utils.randomWord();
		
		URL url = new URL(REST_URL + "insertword/"+randomWord);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "text/plain");

		if (conn.getResponseCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));

		String output;
		String answer = null;
		while ((output = br.readLine()) != null) {
			answer = output;
		}

		conn.disconnect();

		Assert.assertTrue(answer.equals("A palavra "+randomWord.toUpperCase()+" foi cadastrada com sucesso!!!"));

	}
	
	@Test
	public void insertWordThatAllreadyExistsUsingRestServiceTest() throws Exception {
		
		randomWord = Utils.randomWord();
		
		URL url = new URL(REST_URL + "insertword/"+randomWord);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "text/plain");

		if (conn.getResponseCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));

		String output;
		String answer = null;
		while ((output = br.readLine()) != null) {
			answer = output;
		}

		conn.disconnect();

		Assert.assertTrue(answer.equals("A palavra "+randomWord.toUpperCase()+" foi cadastrada com sucesso!!!"));
		
		url = new URL(REST_URL + "insertword/"+randomWord);
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "text/plain");

		Assert.assertTrue(conn.getResponseCode() == 400);
		conn.disconnect();
	}
	
	
	@Test
	public void insertWordAndGetZeroDistanceUsingRestServiceTest() throws Exception {
		
		randomWord = Utils.randomWord();
		
		URL url = new URL(REST_URL + "insertword/"+randomWord);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "text/plain");

		if (conn.getResponseCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));

		String output;
		String answer = null;
		while ((output = br.readLine()) != null) {
			answer = output;
		}

		conn.disconnect();

		Assert.assertTrue(answer.equals("A palavra "+randomWord.toUpperCase()+" foi cadastrada com sucesso!!!"));
		
		url = new URL(REST_URL + "getmindistance?name="+randomWord+"&threshold=0");
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "application/json");
		
		if (conn.getResponseCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
		}
		
		br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));
		
		List<WordDistance> answerList = new ArrayList<WordDistance>();
		while ((output = br.readLine()) != null) {
			Gson gson = new Gson();
			answerList = gson.fromJson(output, new TypeToken<List<WordDistance>>(){}.getType());			
			
		}

		conn.disconnect();

		Assert.assertTrue(!answerList.isEmpty());
		Assert.assertTrue(answerList.get(0).getDistance() == 0);
		Assert.assertTrue(answerList.get(0).getWord1().equals(randomWord.toUpperCase()));
		Assert.assertTrue(answerList.get(0).getWord2().equals(randomWord.toUpperCase()));
		
		
		conn.disconnect();
	}	
	
}
