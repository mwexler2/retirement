/*
* Retirement calculator - Allows one to run various retirement scenarios and see how much money you have to retire on.
*
* Copyright (C) 2016  Michael C. Wexler

* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.

* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.

* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*
* I can be reached at mike.wexler@gmail.com.
*
*/

package name.wexler.retirement;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.core.JsonGenerationException;

import name.wexler.retirement.Asset.Account;
import name.wexler.retirement.Asset.Asset;
import name.wexler.retirement.CashFlowFrequency.*;
import name.wexler.retirement.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.CashFlowInstance.LiabilityCashFlowInstance;
import name.wexler.retirement.CashFlowInstance.SecurityTransaction;
import name.wexler.retirement.CashFlowSource.CashFlowSource;
import name.wexler.retirement.Entity.Company;
import name.wexler.retirement.Entity.Entity;
import name.wexler.retirement.Entity.Person;


/**
 * Created by mwexler on 6/28/16.
 */
public class Retirement {
    private List<Scenario> scenarios;
    private List<Job> jobs;
    private List<Person> people;
    private List<SecurityTransaction> securityTxns;
    private String cashFlowId;
    private String scenarioId;


    Retirement() {
        Locale enUS = Locale.forLanguageTag("en-US");
        Context context = new Context();

        try {
            this.people = Person.readPeople(context);
            Company.readComapnies(context);
            Job.readJobs(context);
            CashFlowFrequency.readCashFlowFrequencies(context);
            Security.readSecurities(context);
            Asset.readAssets(context);
            CashFlowSource.readCashFlowSources(context);
            Account.readAccounts(context);



            this.scenarios = Scenario.readScenarios(context);
        } catch (JsonGenerationException | JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Scenario> getScenarios() {
        return scenarios;
    }

    public Scenario getScenario(String id) {
        for (Scenario scenario : scenarios) {
            if (scenario.getId().equals(id))
            return scenario;
        }
        return null;
    }

    public Collection<Balance> getAssetValues(String scenarioId, String assetId) {
        Scenario scenario = getScenario(scenarioId);
        Collection<Balance> balances = scenario.getAssetValues(assetId);
        return balances;
    }

    public Collection<Balance> getAssetValues(String scenarioId, String assetId, int year) {
        Scenario scenario = getScenario(scenarioId);
        Collection<Balance> balances = scenario.getAssetValues(assetId, year);
        return balances;
    }

    public Collection<Balance> getLiabilityBalances(String scenarioId, String liabilityId) {
        Scenario scenario = getScenario(scenarioId);
        Collection<Balance> balances = scenario.getLiabilityBalances(liabilityId);
        return balances;
    }

    public List<LiabilityCashFlowInstance> getLiabilityCashFlowInstances(String scenarioId, String liabilityId) {
        Scenario scenario = getScenario(scenarioId);
        List<LiabilityCashFlowInstance> cashFlowInstances = scenario.getLiabilityCashFlowInstances(liabilityId);
        return cashFlowInstances;
    }

    public List<CashFlowInstance> getCashFlows(String scenarioId, String cashFlowId) {
        Scenario scenario = getScenario(scenarioId);
        List<CashFlowInstance> cashFlows = scenario.getCashFlows(cashFlowId);
        return cashFlows;
    }

    public List<CashFlowInstance> getCashFlows(String scenarioId, String cashFlowId, int year) {
        Scenario scenario = getScenario(scenarioId);
        List<CashFlowInstance> cashFlows = scenario.getCashFlows(cashFlowId, year);
        return cashFlows;
    }

    public void setScenarios(List<Scenario> scenarios) {
        this.scenarios = scenarios;
    }

    public List<Person> getPeople() {
        return people;
    }

    public void setPeople(List<Person> people) {
        this.people = people;
    }
}

