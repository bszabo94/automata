package core.Mealy;

/*
 * #%L
 * Automata-Core
 * %%
 * Copyright (C) 2016 Faculty of Informatics, University of Debrecen
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.beans.property.SimpleStringProperty;

public class Machine {
	private List<State> states;
	private String id;
	private Set<Character> iAlphabet, oAlphabet;
	private State currState;

	public Machine(String id) throws MachineException {
		if (id == null || id.equals(""))
			throw new MachineException("The Machine must have an ID.");
		this.states = new ArrayList<State>();
		this.id = id;
		this.currState = null;
		this.iAlphabet = new HashSet<Character>();
		this.oAlphabet = new HashSet<Character>();
	}

	public Machine(String id, Set<Character> iAlphabet, Set<Character> oAlphabet) throws MachineException {
		if (id == null)
			throw new MachineException("The Machine must have an ID.");
		this.id = id;
		this.states = new ArrayList<State>();
		this.currState = null;
		this.init(iAlphabet, oAlphabet);
	}

	public SimpleStringProperty getIDProperty() {
		return new SimpleStringProperty(this.id);
	}

	public SimpleStringProperty getNbmOfStatesProperty() {
		return new SimpleStringProperty(Integer.toString(this.states.size()));
	}

	public String getType() {
		return "Mealy";
	}

	public List<State> getStates() {
		return states;
	}

	public State getCurrState() {
		return currState;
	}

	public void addState() {
		this.states.add(new State());
	}

	public void addState(int n) {
		this.states.add(new State(n));
	}

	public String getID() {
		return id;
	}

	public void setID(String id) {
		this.id = id;
	}

	public void setiAlphabet(Set<Character> iAlphabet) {
		this.iAlphabet = iAlphabet;
	}

	public void setoAlphabet(Set<Character> oAlphabet) {
		this.oAlphabet = oAlphabet;
	}

	public void setCurrState(State currState) {
		this.currState = currState;
	}

	public Set<Character> getiAlphabet() {
		return iAlphabet;
	}

	public Set<Character> getoAlphabet() {
		return oAlphabet;
	}

	public void init(Set<Character> iAlphabet, Set<Character> oAlphabet) throws MachineException {
		if (iAlphabet.size() > oAlphabet.size())
			throw new MachineException("Input Alphabet must contain equal or less symbols than the Output Alphabet!");
		this.iAlphabet = iAlphabet;
		this.oAlphabet = oAlphabet;

		List<Character> inputAlphabet = new ArrayList<Character>(iAlphabet);
		List<Character> outputAlphabet = new ArrayList<Character>(oAlphabet);
		List<Integer> range = IntStream.range(0, iAlphabet.size()).boxed().collect(Collectors.toList());

		for (int i = 0; i < iAlphabet.size(); i++)
			this.addState(i);

		for (State currState : this.states) {
			Collections.shuffle(outputAlphabet);
			Collections.shuffle(range);

			for (int i = 0; i < iAlphabet.size(); i++) {
				currState.addTranslation(new Translation(inputAlphabet.get(i), outputAlphabet.get(i), currState,
						this.states.get(range.get(i))));
			}
		}

		this.currState = this.states.get(0);

	}

	public boolean isValid() {
		if (this.currState == null) {
			return false;
		}

		Set<Character> checkIAlphabet = new HashSet<Character>();
		List<Character> checkOAlphabet = new ArrayList<Character>();
		Set<String> checkStateID = new HashSet<String>();
		
		for (State currState : this.states) {
			if(checkStateID.contains(currState.getID()))
				return false;
			checkStateID.add(currState.getID());
			checkIAlphabet.clear();
			checkOAlphabet.clear();
			if (this.iAlphabet.size() != currState.getTranslations().size())
				return false;
			for (Translation currTranslation : currState.getTranslations()) {
				checkIAlphabet.add(currTranslation.getInput());
				checkOAlphabet.add(currTranslation.getOutput());
			}
			if (!this.iAlphabet.equals(checkIAlphabet))
				return false;
			if (checkOAlphabet.size() != new HashSet<Character>(checkOAlphabet).size())
				return false;
			for (Character currChar : checkOAlphabet) {
				if (!this.oAlphabet.contains(currChar))
					return false;
			}
			for (Character currChar : checkIAlphabet) {
				if (!this.iAlphabet.contains(currChar))
					return false;
			}
		}

		return true;
	}

	public Character step(Character input, boolean encoding) {
		if (encoding) {
			for (Translation currTranslation : this.currState.getTranslations()) {
				if (currTranslation.getInput().equals(input)) {
					this.currState = currTranslation.getTarget();
					return currTranslation.getOutput();
				}
			}
		} else {
			for (Translation currTranslation : this.currState.getTranslations()) {
				if (currTranslation.getOutput().equals(input)) {
					this.currState = currTranslation.getTarget();
					return currTranslation.getInput();
				}
			}
		}
		return null;
	}

	public String encode(String input) {
		State temp = this.currState;
		String output = new String();
		for (int i = 0; i < input.length(); i++) {
			output += this.step(input.charAt(i), true);
		}

		// this.currState = this.states.get(0);
		this.currState = temp;

		return output;
	}

	public String decode(String input) {
		State temp = this.currState;
		String output = new String();
		for (int i = 0; i < input.length(); i++) {
			output += this.step(input.charAt(i), false);
		}

		// this.currState = this.states.get(0);
		this.currState = temp;

		return output;

	}

	public void processData(String data) throws MachineException {
		Set<Character> base = new HashSet<Character>();
		for (int i = 0; i < data.length(); i++) {
			base.add(data.charAt(i));
		}
		this.init(base, base);
	}

	public core.Moore.Machine toMoore() throws core.Moore.MachineException {
		core.Moore.Machine m = new core.Moore.Machine(this.id + " --> Moore");
		m.setiAlphabet(new HashSet<Character>(this.iAlphabet));
		m.setoAlphabet(new HashSet<Character>(this.oAlphabet));

		Map<State, Set<Character>> symbolDistributor = new HashMap<State, Set<Character>>();
		for (State currState : this.states) {
			symbolDistributor.put(currState, new HashSet<Character>());
		}

		for (State currState : this.states) {
			for (Translation currTranslation : currState.getTranslations()) {
				symbolDistributor.get(currTranslation.getTarget()).add(currTranslation.getOutput());
			}
		}

		Map<State, Map<Character, core.Moore.State>> translationDistributor = new HashMap<State, Map<Character, core.Moore.State>>();

		for (State currState : symbolDistributor.keySet()) {
			translationDistributor.put(currState, new HashMap<Character, core.Moore.State>());
		}

		int i = 0;

		for (State currState : symbolDistributor.keySet()) {
			for (Character currChar : symbolDistributor.get(currState)) {
				m.addState(currChar, i);
				i++;
				translationDistributor.get(currState).put(currChar, m.getStates().get(m.getStates().size() - 1));
				if (m.getCurrState() == null && this.currState == currState) {
					m.setCurrState(m.getStates().get(m.getStates().size() - 1));
				}
			}
		}

		for (State currState : this.states) {
			for (Translation currTranslation : currState.getTranslations()) {
				for (core.Moore.State tempState : translationDistributor.get(currState).values()) {
					tempState.addTranslation(new core.Moore.Translation(tempState, currTranslation.getInput(),
							translationDistributor.get(currTranslation.getTarget()).get(currTranslation.getOutput())));
				}
			}
		}

		return m;
	}

	public Set<Character> getSymbols(String sentence) {
		Set<Character> symbols = new HashSet<Character>();
		for (int i = 0; i < sentence.length(); i++) {
			symbols.add(sentence.charAt(i));
		}

		return symbols;
	}

	public Map<State, List<Translation>> getTranslations() {
		Map<State, List<Translation>> allTranslation = new HashMap<State, List<Translation>>();

		for (State currState : this.states) {
			allTranslation.put(currState, new ArrayList<Translation>());
			for (Translation currTranslation : currState.getTranslations()) {
				allTranslation.get(currState).add(currTranslation);
			}
		}

		return allTranslation;
	}

	public List<Translation> getTranslationsAsList() {
		List<Translation> translations = new ArrayList<Translation>();

		for (State currState : this.getTranslations().keySet()) {
			for (Translation currTranslation : this.getTranslations().get(currState)) {
				translations.add(currTranslation);
				currTranslation.setParent(currState);
			}
		}

		return translations;
	}

	public void removeState(String id) throws MachineException {
		State s = null;
		for (State currState : this.states)
			if(id.equals(currState.getID())){
				s = currState;
				break;
			}
		if (s == null)
			throw new MachineException("There is no State with the given ID.");
				
		for (State currState : this.states) {
			for (int i = 0; i < currState.getTranslations().size(); i++) {
				if (currState.getTranslations().get(i).getParent() == s
						|| currState.getTranslations().get(i).getTarget() == s) {
					currState.getTranslations().remove(currState.getTranslations().get(i));
					i--;
				}
			}
		}
		this.states.remove(s);
	}

	public String toString() {
		String output = new String();
		output += "Machine: " + this.id + "\n";
		output += "Input Alphabet: " + this.iAlphabet + "\n";
		output += "Output Alphabet: " + this.oAlphabet + "\n";
		for (State currState : this.states) {
			output += "---State " + this.states.indexOf(currState) + "---\n";
			for (Translation currTranslation : currState.getTranslations()) {
				output += "[ " + currTranslation.getParent().getID() + "/" + currTranslation.getInput() + " ---> "
						+ currTranslation.getOutput() + " / q" + this.states.indexOf(currTranslation.getTarget())
						+ " ]\n";
			}
		}
		return output;
	}

}
