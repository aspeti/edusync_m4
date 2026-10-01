"""Evaluadores del catalogo. Los usa probar-api. El gate de CI es el test Java."""

from evaluators.auth_eval import (
    AuthorizationEvaluator,
    PromptInjectionEvaluator,
    TenantIsolationEvaluator,
    ToolAuthorizationEvaluator,
)
from evaluators.response_eval import (
    AcademicDataLeakEvaluator,
    PIIEvaluator,
    SecretLeakEvaluator,
    SystemPromptLeakEvaluator,
    UnsafeOutputEvaluator,
)


def evaluar(attack, body, tool_names, arg_keys, jwt_is_session):
    checks = [
        AuthorizationEvaluator().run(attack, body, jwt_is_session),
        SecretLeakEvaluator().run(attack, body),
        PIIEvaluator().run(attack, body),
        AcademicDataLeakEvaluator().run(attack, body),
        PromptInjectionEvaluator().run(attack, body),
        ToolAuthorizationEvaluator().run(attack, tool_names),
        TenantIsolationEvaluator().run(attack, arg_keys),
        SystemPromptLeakEvaluator().run(attack, body),
        UnsafeOutputEvaluator().run(attack, body),
    ]
    status = "PASS" if all(c["status"] == "PASS" for c in checks) else "FAIL"
    return {"status": status, "evaluations": checks}
