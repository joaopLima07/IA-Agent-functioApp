import { useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import { CheckCircle2, Loader2 } from "lucide-react";
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
      setError(result.error.issues[0]?.message ?? "Enter a valid email");
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
      <Card>
        <CardContent className="flex flex-col items-center gap-3 pt-6 text-center">
          <div className="flex size-10 items-center justify-center rounded-full bg-success/10 text-success">
            <CheckCircle2 className="size-5" />
          </div>
          <div>
            <p className="text-sm font-medium">Check your inbox</p>
            <p className="mt-1 text-sm text-muted-foreground">
              If an account exists for <span className="font-medium text-foreground">{email}</span>, we've sent a
              link to reset your password.
            </p>
          </div>
          <Button asChild variant="outline" className="mt-2 w-full">
            <Link to="/login">Back to sign in</Link>
          </Button>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Reset your password</CardTitle>
        <CardDescription>We'll email you a link to reset your password.</CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4" noValidate>
          <div className="grid gap-1.5">
            <Label htmlFor="email">Email</Label>
            <Input id="email" type="email" autoComplete="email" value={email} onChange={(e) => setEmail(e.target.value)} aria-invalid={!!error} />
            {error && <p className="text-xs text-destructive">{error}</p>}
          </div>
          <Button type="submit" className="w-full" disabled={isSubmitting}>
            {isSubmitting && <Loader2 className="size-4 animate-spin" />}
            Send reset link
          </Button>
        </form>
        <p className="mt-6 text-center text-sm text-muted-foreground">
          <Link to="/login" className="font-medium text-primary hover:underline">
            Back to sign in
          </Link>
        </p>
      </CardContent>
    </Card>
  );
}
