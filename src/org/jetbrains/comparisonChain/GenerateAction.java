package org.jetbrains.comparisonChain;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.List;

public class GenerateAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        final PsiClass psiClass = getPsiClassFromContext(e);
        GenerateDialog dlg = new GenerateDialog(psiClass);
        dlg.show();

        if (dlg.isOK()) {
            generateComparable(psiClass, dlg.getFields());
        }
    }

    private void generateComparable(PsiClass psiClass, List<PsiField> fields) {
        new WriteCommandAction.Simple(psiClass.getProject(), psiClass.getContainingFile()) {
            @Override
            protected void run() throws Throwable {
                generateCompareTo(psiClass, fields);
                generateImplementsComparable(psiClass);
            }

        }.execute();
    }

    private void generateCompareTo(PsiClass psiClass, List<PsiField> fields) {
        final StringBuilder builder = new StringBuilder("public int compareTo(");
        builder.append(psiClass.getName()).append(" that) {\n");
        builder.append("return com.google.common.collect.ComparisonChain.start()");
        for (PsiField field : fields) {
            builder.append(".compare(this.").append(field.getName()).append(", that.").append(field.getName()).append(")");
        }
        builder.append(".result();\n}");
        final PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiClass.getProject());
        final PsiMethod compareTo = elementFactory.createMethodFromText(builder.toString(), psiClass);
        final PsiElement method = psiClass.add(compareTo);
        JavaCodeStyleManager.getInstance(psiClass.getProject()).shortenClassReferences(method);
    }

    private void generateImplementsComparable(PsiClass psiClass) {
        final PsiClassType[] implementsListTypes = psiClass.getImplementsListTypes();
        for (PsiClassType implementsListType : implementsListTypes) {
            final PsiClass resolved = implementsListType.resolve();
            if (null != resolved && "java.lang.Comparable".equals(resolved.getQualifiedName())) {
                return;
            }
        }

        String implementsType = "Comparable<" + psiClass.getName() + ">";
        final PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiClass.getProject());
        final PsiJavaCodeReferenceElement referenceElement = elementFactory.createReferenceFromText(implementsType, psiClass);
        final PsiElement implementsList = psiClass.getImplementsList();
        if (null != implementsList) {
            implementsList.add(referenceElement);
        }
    }

    @Override
    public void update(AnActionEvent e) {
        final PsiClass psiClass = getPsiClassFromContext(e);
        e.getPresentation().setEnabled(null != psiClass);
    }

    private PsiClass getPsiClassFromContext(AnActionEvent e) {
        final PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        final Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (null == psiFile || null == editor) {
            return null;
        }
        final int offset = editor.getCaretModel().getOffset();
        final PsiElement element = psiFile.findElementAt(offset);
        return PsiTreeUtil.getParentOfType(element, PsiClass.class);
    }
}
