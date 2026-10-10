import { useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import { CheckCircle2, Loader2, Mail } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { authService } from "@/features/auth/services/authService";
import { forgotPasswordSchema } from "@/features/auth/types/schemas";

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState("");
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [sent, setSent] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    const result = forgotPasswordSchema.safeParse({ email });
    if (!result.success) {
      setError(result.error.issues[0]?.message ?? "Digite um e-mail válido");
      return;
    }
    setError("");
    setIsSubmitting(true);
    await authService.forgotPassword(result.data.email);
    setIsSubmitting(false);
    setSent(true);
  };

  if (sent) {
    return (
      <Card className="border-border/60 shadow-lg">
        <CardContent className="flex flex-col items-center gap-3 pt-6 text-center">
          <div className="flex size-10 items-center justify-center rounded-full bg-success/10 text-success">
            <CheckCircle2 className="size-5" />
          </div>
          <div>
            <p className="text-sm font-medium">Verifique sua caixa de entrada</p>
            <p className="mt-1 text-sm text-muted-foreground">
              Se existir uma conta para <span className="font-medium text-foreground">{email}</span>, enviamos um
              link para redefinir sua senha.
            </p>
          </div>
          <Button asChild variant="outline" className="mt-2 w-full">
            <Link to="/login">Voltar para o login</Link>
          </Button>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className="border-border/60 shadow-lg">
      <CardHeader className="gap-1.5 pb-2">
        <CardTitle className="text-xl">Redefinir sua senha</CardTitle>
        <CardDescription>Enviaremos um e-mail com um link para redefinir sua senha.</CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4" noValidate>
          <div className="grid gap-1.5">
            <Label htmlFor="email">E-mail</Label>
            <div className="relative">
              <Mail className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                id="email"
                type="email"
                autoComplete="email"
                placeholder="you@company.com"
                className="pl-9"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                aria-invalid={!!error}
              />
            </div>
            {error && <p className="text-xs text-destructive">{error}</p>}
          </div>
          <Button type="submit" className="w-full" disabled={isSubmitting}>
            {isSubmitting && <Loader2 className="size-4 animate-spin" />}
            Enviar link de redefinição
          </Button>
        </form>
        <p className="mt-6 text-center text-sm text-muted-foreground">
          <Link to="/login" className="font-medium text-primary hover:underline">
            Voltar para o login
          </Link>
        </p>
      </CardContent>
    </Card>
  );
}
