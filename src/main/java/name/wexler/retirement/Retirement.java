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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.core.JsonGenerationException;

import name.wexler.retirement.CashFlow.*;


/**
 * Created by mwexler on 6/28/16.
 */
public class Retirement {
    private Scenario[] scenarios;
    private Job[] jobs;
    private Entity[] companies;
    private Entity[] people;
    private CashFlowSource[] cashFlowSources;
    private Security[] securities;
    private Asset[] assets;
    private Assumptions assumptions;
    private CashFlowFrequency[] cashFlows;
    private String cashFlowId;
    private String scenarioId;


    Retirement() {
        Locale enUS = Locale.forLanguageTag("en-US");
        Context context = new Context();

        try {

            String userHome = System.getProperty("user.home");
            String resourceDir = userHome + "/.retirement/resources";
            
            String peoplePath = resourceDir + "/people.json";
            this.people = context.fromJSONFileArray(Entity[].class, peoplePath);

            String companyPath = resourceDir + "/company.json";
            this.companies = context.fromJSONFile(Entity[].class, companyPath);

            String jobsPath = resourceDir + "/jobs.json";
            this.jobs = context.fromJSONFileArray(Job[].class, jobsPath);

            String cashFlowsPath = resourceDir + "/cashFlows.json";
            this.cashFlows = context.fromJSONFileArray(CashFlowFrequency[].class, cashFlowsPath);

            String securitiesPath = resourceDir + "/securities.json";
            this.securities = context.fromJSONFileArray(Security[].class, securitiesPath);

            String assetsPath = resourceDir + "/assets.json";
            this.assets = context.fromJSONFileArray(Asset[].class, assetsPath);

            String cashFlowSourcesPath = resourceDir + "/cashFlowSources.json";
            this.cashFlowSources = context.fromJSONFileArray(CashFlowSource[].class, cashFlowSourcesPath);

            String assumptionsPath = resourceDir + "/assumptions.json";
            this.assumptions = context.fromJSONFile(Assumptions.class, assumptionsPath);
            context.setAssumptions(this.assumptions);

            String scenariosPath = resourceDir + "/scenarios.json";
            this.scenarios = context.fromJSONFileArray(Scenario[].class, scenariosPath);
        } catch (JsonGenerationException | JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Scenario[] getScenarios() {
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

    public void setScenarios(Scenario[] scenarios) {
        this.scenarios = scenarios;
    }

    public Entity[] getPeople() {
        return people;
    }

    public void setPeople(Person[] people) {
        this.people = people;
    }
}

