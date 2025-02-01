import { StyleSheet } from 'react-native';

export const colors = {
  primary: '#ffffff',
  secondary: '#f5f5f5',
  text: '#333333',
  textLight: '#666666',
  border: '#e0e0e0',
  buttonBg: '#f0f0f0',
  success: '#4CAF50',
  danger: '#f44336',
};

export const spacing = {
  xs: 4,
  sm: 8,
  md: 16,
  lg: 24,
  xl: 32,
};

export const typography = {
  fontFamily: 'System',
  sizes: {
    small: 12,
    regular: 14,
    medium: 16,
    large: 18,
    xlarge: 24,
  },
};

export const globalStyles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.primary,
  },
  input: {
    height: 40,
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: 8,
    paddingHorizontal: spacing.md,
    marginVertical: spacing.sm,
    fontFamily: typography.fontFamily,
    fontSize: typography.sizes.regular,
  },
  button: {
    backgroundColor: colors.buttonBg,
    padding: spacing.md,
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
  },
  buttonText: {
    color: colors.text,
    fontSize: typography.sizes.regular,
    fontFamily: typography.fontFamily,
  },
  card: {
    backgroundColor: colors.primary,
    borderRadius: 8,
    padding: spacing.md,
    marginVertical: spacing.sm,
    borderWidth: 1,
    borderColor: colors.border,
  },
  title: {
    fontSize: typography.sizes.large,
    fontWeight: 'bold',
    color: colors.text,
    marginBottom: spacing.md,
  },
  subtitle: {
    fontSize: typography.sizes.medium,
    color: colors.textLight,
    marginBottom: spacing.sm,
  },
}); 