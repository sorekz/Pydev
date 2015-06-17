package com.python.pydev.analysis.search_index;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.python.pydev.core.IPythonNature;
import org.python.pydev.core.log.Log;
import org.python.pydev.plugin.nature.PythonNature;
import org.python.pydev.shared_core.string.StringMatcher;
import org.python.pydev.shared_core.string.StringUtils;
import org.python.pydev.shared_ui.search.ScopeAndData;
import org.python.pydev.shared_ui.search.SearchIndexData;

public class PyScopeAndData {

    public static List<IPythonNature> getPythonNatures(ScopeAndData scopeAndData) {
        if (scopeAndData.scope == SearchIndexData.SCOPE_PROJECTS) {
            StringMatcher[] matchers = SearchResultsViewerFilter.createMatchers(scopeAndData.scopeData);
            ArrayList<IPythonNature> ret = new ArrayList<>();
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            for (IProject project : workspace.getRoot().getProjects()) {
                if (project != null && project.exists() && project.isOpen()) {
                    if (SearchResultsViewerFilter.filterMatches(project.getName(), matchers)) {
                        ret.add(PythonNature.getPythonNature(project));
                    }
                }
            }
            if (ret.size() == 0) {
                Log.log("Unable to resolve projects to search from string: '" + scopeAndData.scopeData
                        + "' (searching workspace).");
                ret.addAll(PythonNature.getAllPythonNatures());
            }
            return ret;
        }

        if (scopeAndData.scope == SearchIndexData.SCOPE_MODULES) {
            ArrayList<IPythonNature> ret = new ArrayList<>();

            StringMatcher[] matchers = SearchResultsViewerFilter.createMatchers(scopeAndData.scopeData);

            List<IPythonNature> allPythonNatures = PythonNature.getAllPythonNatures();
            for (IPythonNature nature : allPythonNatures) {
                Set<String> allModuleNames = nature.getAstManager().getModulesManager().getAllModuleNames(false, "");
                for (String s : allModuleNames) {
                    if (SearchResultsViewerFilter.filterMatches(s, matchers)) {
                        ret.add(nature);
                        break;
                    }
                }
            }
            return ret;
        }
        if (scopeAndData.scope == SearchIndexData.SCOPE_WORKSPACE) {
            return PythonNature.getAllPythonNatures();
        }

        Log.log("Unable to deal with scope: " + scopeAndData.scope + ". Searching workspace.");
        return PythonNature.getAllPythonNatures();
    }

    public static Set<String> getModuleNamesFilter(ScopeAndData scopeAndData) {
        if (scopeAndData.scope == SearchIndexData.SCOPE_MODULES) {
            List<String> split = StringUtils.split(scopeAndData.scopeData, ',');
            Set<String> set = new HashSet<>(split.size());
            for (String string : split) {
                string = string.trim();
                if (string.length() > 0) {
                    set.add(string);
                }
            }
            return set;
        }
        return new HashSet<>(1);
    }

}